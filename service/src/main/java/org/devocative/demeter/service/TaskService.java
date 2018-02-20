package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.entity.DTaskInfo;
import org.devocative.demeter.entity.DTaskSchedule;
import org.devocative.demeter.filter.CollectionUtil;
import org.devocative.demeter.iservice.*;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.*;
import org.devocative.demeter.vo.DTaskVO;
import org.devocative.demeter.vo.core.DModuleInfoVO;
import org.devocative.demeter.vo.core.DTaskInfoVO;
import org.devocative.demeter.vo.filter.DTaskFVO;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service("dmtTaskService")
public class TaskService implements ITaskService, IApplicationLifecycle, RejectedExecutionHandler, ITaskResultEvent {
	private static Logger logger = LoggerFactory.getLogger(TaskService.class);

	private boolean enabled;
	private final Map<String, DTask> TASKS = new ConcurrentHashMap<>();
	private Scheduler scheduler;
	private ThreadPoolExecutor threadPoolExecutor;
	private Map<String, IRequestLifecycle> requestLifecycleBeans;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IDemeterCoreService demeterCoreService;

	// ------------------------------ IApplicationLifecycle

	@Override
	public void init() {
		enabled = ConfigUtil.getBoolean(DemeterConfigKey.TaskEnabled);
		logger.info("TaskService.init(): enabled={}", enabled);

		if (!enabled) {
			return;
		}

		List<Long> validIds = new ArrayList<>();
		List<DModuleInfoVO> modules = demeterCoreService.getModules();
		for (DModuleInfoVO xModule : modules) {
			if (xModule.getTasks() != null) {
				for (DTaskInfoVO xdTask : xModule.getTasks()) {
					try {
						Class<?> beanType = Class.forName(xdTask.getType());
						demeterCoreService.getBean(beanType);
					} catch (ClassNotFoundException e) {
						throw new DSystemException("Unknown task type as bean: " + xdTask.getType());
					}

					DTaskInfo dTaskInfo = addOrUpdateTask(xModule.getShortName().toLowerCase(), xdTask);
					validIds.add(dTaskInfo.getId());
				}
			}
		}

		Long count = persistorService.createQueryBuilder()
			.addSelect("select count(1) from DTaskInfo")
			.object();
		if (validIds.size() < count) {
			int noOfDisables = persistorService
				.createQueryBuilder()
				.addSelect("update DTaskInfo ent set ent.enabled = false where ent.id not in (:validIds)")
				.addParam("validIds", validIds)
				.update();

			logger.warn("DTaskInfo are disabled: count=[{}] dbAffect=[{}]", count - validIds.size(), noOfDisables);
		}

		persistorService.commitOrRollback();

		requestLifecycleBeans = demeterCoreService.getBeansOfType(IRequestLifecycle.class);

		threadPoolExecutor = new DemeterThreadPoolExecutor(
			ConfigUtil.getInteger(DemeterConfigKey.TaskPoolSize),
			ConfigUtil.getInteger(DemeterConfigKey.TaskPoolMax),
			ConfigUtil.getInteger(DemeterConfigKey.TaskPoolAliveTime),
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>());

		threadPoolExecutor.setRejectedExecutionHandler(this);

		logger.info("TaskService.init(): ThreadPoolExecutor Up!");

		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			logger.info("TaskService.init(): Scheduler Up!");
		} catch (SchedulerException e) {
			logger.error("TaskService.init(): StdSchedulerFactory: ", e);
			throw new DSystemException("TaskService.init(): StdSchedulerFactory", e);
		}

		List<DTaskSchedule> list = persistorService
			.createQueryBuilder()
			.addFrom(DTaskSchedule.class, "ent")
			.addWhere("and ent.enabled = true and ent.task.enabled = true")
			.list();
		for (DTaskSchedule schedule : list) {
			schedule(schedule);
		}
	}

	@Override
	public void shutdown() {
		if (!enabled) {
			return;
		}

		stopAll();

		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			logger.warn("Scheduler Shutdown:", e);
		}
		threadPoolExecutor.shutdown();
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Third;
	}

	// ------------------------------ RejectedExecutionHandler

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		logger.error("*** TaskService.RejectedExecution: {}", r);
	}

	// ------------------------------ ITaskService

	@Override
	public DTaskInfo load(Long id) {
		return persistorService.get(DTaskInfo.class, id);
	}

	@Override
	public DTaskInfo loadByType(String type) {
		return persistorService
			.createQueryBuilder()
			.addFrom(DTaskInfo.class, "ent")
			.addWhere("and ent.type = :type")
			.addParam("type", type)
			.object();
	}

	@Override
	public List<DTaskInfo> search(long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addFrom(DTaskInfo.class, "ent")
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count() {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(DTaskInfo.class, "ent")
			.object();
	}

	// ==============================

	@Override
	public DTaskResult start(Class<? extends DTask> taskBeanClass, Object inputData, ITaskResultCallback resultCallback) {
		return start(taskBeanClass, null, inputData, resultCallback);
	}

	@Override
	public DTaskResult start(Class<? extends DTask> taskBeanClass, Object id, Object inputData, ITaskResultCallback resultCallback) {
		if (!enabled) {
			throw new DSystemException("Task handling is not enabled");
		}

		logger.info("Starting Task: class=[{}] - id=[{}] - inputData=[{}]", taskBeanClass, id, inputData);

		return start(loadByType(taskBeanClass.getName()), id, inputData, resultCallback);
	}

	@Override
	public DTaskResult start(Long taskInfoId, Object id, Object inputData, ITaskResultCallback resultCallback) {
		if (!enabled) {
			throw new DSystemException("Task handling is not enabled");
		}

		return start(load(taskInfoId), id, inputData, resultCallback);
	}

	@Override
	public void stop(Class<? extends DTask> taskBeanClass, Object id) {
		String key = taskBeanClass.getName();
		if (id != null) {
			key += "_" + id;
		}
		stop(key);
	}

	@Override
	public void stop(String key) {
		if (TASKS.containsKey(key)) {
			try {
				TASKS.get(key).stop();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("DTask Not Found: " + key);
		}
	}

	@Override
	public void stopAll() {
		logger.info("TaskService.Shutdown: Running Tasks Count=[{}]", TASKS.size());

		for (DTask dTask : TASKS.values()) {
			try {
				dTask.stop();
			} catch (Exception e) {
				logger.error("TaskService.Shutdown: id=[{}] key=[{}]", dTask.getId(), dTask.getKey(), e);
			}
		}
	}

	// ---------------

	@Override
	public List<DTaskVO> search(DTaskFVO dTaskFVO, long pageIndex, long pageSize) {
		List<DTaskVO> dTaskVOs = CollectionUtil.filterCollection(getRunningTasks(), dTaskFVO);
		int toIndex = (int) (pageIndex * pageSize);
		return dTaskVOs.subList((int) ((pageIndex - 1) * pageIndex), Math.min(toIndex, dTaskVOs.size()));
	}

	@Override
	public long count(DTaskFVO dTaskFVO) {
		return CollectionUtil.filterCollection(getRunningTasks(), dTaskFVO).size();
	}

	@Override
	public void attachToCallback(String key, ITaskResultCallback callback) {
		if (TASKS.containsKey(key)) {
			List<ITaskResultCallback> list = TASKS.get(key).getResultCallbacks();
			if (!list.contains(callback)) {
				list.add(callback);
				logger.info("Attach to Task Result: {}", key);
			} else {
				throw new RuntimeException("Already Attached Callback Handler");
			}
		} else {
			throw new RuntimeException("Invalid key of DTask");
		}
	}

	@Override
	public void detachFromCallback(String key, ITaskResultCallback callback) {
		if (TASKS.containsKey(key)) {
			List<ITaskResultCallback> list = TASKS.get(key).getResultCallbacks();
			boolean result = list.remove(callback);
			if (!result) {
				throw new RuntimeException("Already Detached Callback Handler");
			} else {
				logger.info("Detach from Task Result: {}", key);
			}
		} else {
			throw new RuntimeException("Invalid key of DTask");
		}
	}

	// ---------------

	@Override
	public void onTaskResult(DTask<?> dTask, Object result) {
		securityService.authenticate(dTask.getCurrentUser());
		for (ITaskResultCallback callback : dTask.getResultCallbacks()) {
			try {
				callback.onTaskResult(dTask.getId(), result);
			} catch (Exception e) {
				callback.onTaskError(dTask.getId(), e);
			}
		}
		securityService.authenticate(dTask.getCurrentUser());
	}

	@Override
	public void onTaskError(DTask<?> dTask, Exception e) {
		securityService.authenticate(dTask.getCurrentUser());
		for (ITaskResultCallback callback : dTask.getResultCallbacks()) {
			try {
				callback.onTaskError(dTask.getId(), e);
			} catch (Exception e1) {
				logger.error("TaskService.onTaskError: dTask={}", dTask, e1);
			}
		}
		securityService.authenticate(dTask.getCurrentUser());
	}

	// ------------------------------ Private

	private List<DTaskVO> getRunningTasks() {
		List<DTaskVO> result = new ArrayList<>(TASKS.size());
		for (DTask dTask : TASKS.values()) {
			result.add(new DTaskVO(
				dTask.getId().toString(),
				dTask.getClass().getName(),
				dTask.getStartDate(),
				dTask.getState(),
				dTask.getCurrentUser().getUsername()
			));
		}
		return result;
	}

	private DTaskResult start(DTaskInfo taskInfo, Object id, Object inputData, ITaskResultCallback resultCallback) {
		if (taskInfo.getEnabled()) {
			try {
				Class taskClass = Class.forName(taskInfo.getType());

				logger.info("Starting Task: class=[{}] - taskInfo=[{}]", taskClass, taskInfo.getId());

				DTask dTask = (DTask) demeterCoreService.getBean(taskClass);
				return startDTask(dTask, id, inputData, resultCallback);
			} catch (ClassNotFoundException e) {
				logger.error("Can't find task class: class=[{}] taskInfo=[{}]", taskInfo.getType(), taskInfo.getId());
				throw new RuntimeException(e); //TODO
			}
		} else {
			logger.warn("Executing disabled task: {}", taskInfo.getType());
		}

		return null;
	}

	// Main Start DTask Method
	private DTaskResult startDTask(DTask dTask, Object id, Object inputData, ITaskResultCallback resultCallback) {
		if (id == null) {
			id = String.valueOf(System.currentTimeMillis()); //TODO using DTaskLog.id
		}

		dTask
			.setId(id)
			.setInputData(inputData)
			.setTaskResultEvent(this)
			.addTaskResultCallback(resultCallback)
			.setCurrentUser(securityService.getCurrentUser());

		Future<?> result = null;
		if (TASKS.containsKey(dTask.getKey())) {
			logger.warn("ReRunning Task: {}", dTask.getKey());
		} else {
			TASKS.put(dTask.getKey(), dTask);
			result = threadPoolExecutor.submit(dTask);
			logger.info("Started Task: {}", dTask.getKey());
		}
		return new DTaskResult(result, dTask);
	}

	private DTaskInfo addOrUpdateTask(String module, DTaskInfoVO xdTask) {
		DTaskInfo dTaskInfo = persistorService
			.createQueryBuilder()
			.addFrom(DTaskInfo.class, "ent")
			.addWhere("and ent.type = :type")
			.addParam("type", xdTask.getType())
			.object();

		if (dTaskInfo == null) {
			dTaskInfo = new DTaskInfo();
			dTaskInfo.setType(xdTask.getType());
			dTaskInfo.setModule(module);
			persistorService.saveOrUpdate(dTaskInfo);
		}

		if (xdTask.getCronExpression() != null) {
			DTaskSchedule schedule = persistorService
				.createQueryBuilder()
				.addSelect("from DTaskSchedule ent")
				.addWhere("and ent.refId is null")
				.addWhere("and ent.task.id = :taskId")
				.addParam("taskId", dTaskInfo.getId())
				.object();
			if (schedule == null) {
				schedule = new DTaskSchedule();
				schedule.setTask(dTaskInfo);
			}
			schedule.setCronExpression(xdTask.getCronExpression());
			persistorService.saveOrUpdate(schedule);
		}

		return dTaskInfo;
	}

	private void schedule(DTaskSchedule taskSchedule) {
		String jobKey = taskSchedule.getId().toString();
		logger.info("DTaskSchedule: scheduling: {}", jobKey);

		JobDataMap map = new JobDataMap();
		map.put(IDemeterCoreService.JOB_KEY, demeterCoreService);

		JobDetail job = newJob(DTaskScheduleJob.class)
			.withIdentity(JobKey.jobKey(jobKey))
			.setJobData(map)
			.build();

		//TODO use DTaskSchedule calendar
		Trigger trigger;
		trigger = newTrigger()
			.withIdentity(jobKey)
			.withSchedule(CronScheduleBuilder.cronSchedule(taskSchedule.getCronExpression()))
			.forJob(job)
			.startNow()
			.build();

		try {
			scheduler.scheduleJob(job, trigger);
			logger.info("DTaskSchedule: scheduled: {}", jobKey);
		} catch (SchedulerException e) {
			logger.error("TaskService.schedule(): " + jobKey, e);
		}
	}

	// ------------------------------ Demeter ThreadPoolExecutor & DemeterFutureTask

	private class DemeterThreadPoolExecutor extends ThreadPoolExecutor {
		public DemeterThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
										 BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			DemeterFutureTask futureTask = (DemeterFutureTask) r;
			DTask task = futureTask.getDTask();
			securityService.authenticate(task.getCurrentUser());

			if (requestLifecycleBeans != null) {
				for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
					try {
						logger.debug("Before of TaskService.ThreadPoolExecutor.beforeRequest(): bean = {}",
							requestLifecycle.getClass().getName());
						requestLifecycle.beforeRequest();
					} catch (Exception e) {
						logger.error("TaskService.ThreadPoolExecutor.beforeRequest(): bean = {}",
							requestLifecycle.getClass().getName(), e);
					}
				}
			}
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			DemeterFutureTask futureTask = (DemeterFutureTask) r;
			DTask dTask = futureTask.getDTask();
			logger.info("Task finished: key=[{}], state=[{}], duration=[{}]", dTask.getKey(), dTask.getState(), dTask.getDuration());
			TASKS.remove(dTask.getKey());

			if (requestLifecycleBeans != null) {
				for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
					try {
						logger.debug("Before of TaskService.ThreadPoolExecutor.afterResponse(): bean = {}",
							requestLifecycle.getClass().getName());
						requestLifecycle.afterResponse();
					} catch (Exception e) {
						logger.error("TaskService.ThreadPoolExecutor.afterResponse(): bean = {}",
							requestLifecycle.getClass().getName(), e);
					}
				}
			}
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
			return new DemeterFutureTask<>(runnable, value);
		}
	}

	private class DemeterFutureTask<T> extends FutureTask<T> {
		private Runnable mainRunnable;

		public DemeterFutureTask(Runnable runnable, T result) {
			super(runnable, result);

			this.mainRunnable = runnable;
		}

		public DTask getDTask() {
			return (DTask) mainRunnable;
		}
	}
}
