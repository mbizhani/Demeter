package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.core.xml.XDTask;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.entity.DTaskInfo;
import org.devocative.demeter.entity.DTaskSchedule;
import org.devocative.demeter.filter.CollectionUtil;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.IRequestLifecycle;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.*;
import org.devocative.demeter.vo.DTaskVO;
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
	private final Map<String, List<ITaskResultCallback>> TASKS_CALLBACK = new ConcurrentHashMap<>();
	private Scheduler scheduler;
	private ThreadPoolExecutor threadPoolExecutor;
	private Map<String, IRequestLifecycle> requestLifecycleBeans;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------ IApplicationLifecycle

	@Override
	public void init() {
		enabled = ConfigUtil.getBoolean(DemeterConfigKey.TaskEnabled);
		logger.info("TaskService.init(): enabled={}", enabled);

		if (!enabled) {
			return;
		}

		threadPoolExecutor = new DemeterThreadPoolExecutor(
			ConfigUtil.getInteger(DemeterConfigKey.TaskPoolSize),
			ConfigUtil.getInteger(DemeterConfigKey.TaskPoolMax),
			ConfigUtil.getInteger(DemeterConfigKey.TaskPoolAliveTime),
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>());

		threadPoolExecutor.setRejectedExecutionHandler(this);

		logger.info("TaskService.init(): ThreadPoolExecutor Up!");

		requestLifecycleBeans = DemeterCore.get().getApplicationContext().getBeansOfType(IRequestLifecycle.class);

		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			logger.info("TaskService.init(): Scheduler Up!");
		} catch (SchedulerException e) {
			logger.error("TaskService.init(): StdSchedulerFactory: ", e);
			throw new DSystemException("TaskService.init(): StdSchedulerFactory", e);
		}

		persistorService
			.createQueryBuilder()
			.addSelect("update DTaskInfo ent set ent.enabled=false")
			.update();
		persistorService.commitOrRollback();

		Map<String, XModule> modules = DemeterCore.get().getModules();
		for (XModule xModule : modules.values()) {
			if (xModule.getTasks() != null) {
				for (XDTask xdTask : xModule.getTasks()) {
					try {
						Class<?> beanType = Class.forName(xdTask.getType());
						DemeterCore.get().getApplicationContext().getBean(beanType);
					} catch (ClassNotFoundException e) {
						throw new DSystemException("Unknown task type as bean: " + xdTask.getType());
					}

					addOrUpdateTask(xModule.getShortName().toLowerCase(), xdTask);
				}
			}
		}

		List<DTaskSchedule> list = persistorService
			.createQueryBuilder()
			.addFrom(DTaskSchedule.class, "ent")
			.addWhere("and ent.enabled=true and ent.task.enabled=true")
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

		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			logger.warn("Scheduler shutdown:", e);
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
			TASKS.get(key).stop();
		} else {
			throw new RuntimeException("DTask Not Found: " + key);
		}
	}

	@Override
	public void stopAll() {
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
		if (TASKS_CALLBACK.containsKey(key)) {
			List<ITaskResultCallback> list = TASKS_CALLBACK.get(key);
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
		if (TASKS_CALLBACK.containsKey(key)) {
			List<ITaskResultCallback> list = TASKS_CALLBACK.get(key);
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
	public void onTaskResult(DTask dTask, Object result) {
		securityService.authenticate(dTask.getCurrentUser());

		List<ITaskResultCallback> callbacks = TASKS_CALLBACK.get(dTask.getKey());
		List<ITaskResultCallback> toRemove = new ArrayList<>();

		for (ITaskResultCallback callback : callbacks) {
			try {
				callback.onTaskResult(dTask.getId(), result);
			} catch (DemeterException e) {
				if (DemeterErrorCode.InvalidPushConnection.equals(e.getErrorCode())) {
					toRemove.add(callback);
				} else {
					callback.onTaskError(dTask.getId(), e);
				}
			} catch (Exception e) {
				callback.onTaskError(dTask.getId(), e);
			}
		}

		if (!toRemove.isEmpty()) {
			synchronized (TASKS_CALLBACK.get(dTask.getKey())) {
				logger.info("Remove Disconnected Task Result: key={} size={}", dTask.getKey(), toRemove.size());
				TASKS_CALLBACK.get(dTask.getKey()).removeAll(toRemove);
			}
		}

		securityService.authenticate(dTask.getCurrentUser());
	}

	@Override
	public void onTaskError(DTask dTask, Exception e) {
		securityService.authenticate(dTask.getCurrentUser());

		List<ITaskResultCallback> callbacks = TASKS_CALLBACK.get(dTask.getKey());
		List<ITaskResultCallback> toRemove = new ArrayList<>();

		for (ITaskResultCallback callback : callbacks) {
			try {
				callback.onTaskError(dTask.getId(), e);
			} catch (DemeterException e1) {
				if (DemeterErrorCode.InvalidPushConnection.equals(e1.getErrorCode())) {
					toRemove.add(callback);
				} else {
					logger.error("TaskService.onTaskError: ", e1);
				}
			} catch (Exception e1) {
				logger.error("TaskService.onTaskError: ", e1);
			}
		}

		if (!toRemove.isEmpty()) {
			synchronized (TASKS_CALLBACK.get(dTask.getKey())) {
				logger.info("Remove Disconnected Task Result: key={} size={}", dTask.getKey(), toRemove.size());
				TASKS_CALLBACK.get(dTask.getKey()).removeAll(toRemove);
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

	// Main
	private DTaskResult start(DTaskInfo taskInfo, Object id, Object inputData, ITaskResultCallback resultCallback) {
		if (taskInfo.getEnabled()) {
			try {
				Class taskClass = Class.forName(taskInfo.getType());

				logger.info("Starting Task: class=[{}] - taskInfo=[{}]", taskClass, taskInfo.getId());

				DTask dTask = (DTask) DemeterCore.get().getApplicationContext().getBean(taskClass);
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

	// Main start DTask Method
	private DTaskResult startDTask(DTask dTask, Object id, Object inputData, ITaskResultCallback resultCallback) {
		if (id == null) {
			id = String.valueOf(System.currentTimeMillis()); //TODO using DTaskLog.id
		}

		dTask
			.setId(id)
			.setInputData(inputData)
			.setTaskResultEvent(this)
			.setCurrentUser(securityService.getCurrentUser());

		Future<?> result = null;
		if (TASKS.containsKey(dTask.getKey())) {
			logger.warn("ReRunning Task: {}", dTask.getKey());
		} else {
			TASKS.put(dTask.getKey(), dTask);
			TASKS_CALLBACK.put(dTask.getKey(), new ArrayList<>());
			if (resultCallback != null) {
				TASKS_CALLBACK.get(dTask.getKey()).add(resultCallback);
			}
			result = threadPoolExecutor.submit(dTask);
			logger.info("Started Task: {}", dTask.getKey());
		}
		return new DTaskResult(result, dTask);
	}

	private void addOrUpdateTask(String module, XDTask xdTask) {
		DTaskInfo dTaskInfo = persistorService
			.createQueryBuilder()
			.addFrom(DTaskInfo.class, "ent")
			.addWhere("and ent.type=:type")
			.addParam("type", xdTask.getType())
			.object();

		if (dTaskInfo == null) {
			dTaskInfo = new DTaskInfo();
			dTaskInfo.setType(xdTask.getType());
			dTaskInfo.setModule(module);
			persistorService.saveOrUpdate(dTaskInfo);

			if (xdTask.getCronExpression() != null) {
				DTaskSchedule schedule = new DTaskSchedule();
				schedule.setCronExpression(xdTask.getCronExpression());
				schedule.setTask(dTaskInfo);
				persistorService.saveOrUpdate(schedule);
			}
		} else {
			persistorService
				.createQueryBuilder()
				.addSelect("update DTaskInfo ent set ent.enabled=true")
				.addWhere("and ent.type=:type")
				.addParam("type", xdTask.getType())
				.update();

			if (xdTask.getCronExpression() != null) {
				persistorService
					.createQueryBuilder()
					.addSelect("update DTaskSchedule ent set ent.cronExpression=:crExpr")
					.addWhere("and ent.refId is null")
					.addWhere("and ent.task.id=:taskId")
					.addParam("taskId", dTaskInfo.getId())
					.addParam("crExpr", xdTask.getCronExpression())
					.update();
			}
		}

		persistorService.commitOrRollback();
	}

	private void schedule(DTaskSchedule taskSchedule) {
		String jobKey = taskSchedule.getId().toString();
		logger.info("DTaskSchedule: scheduling: {}", jobKey);

		JobDetail job = newJob(DTaskScheduleJob.class)
			.withIdentity(JobKey.jobKey(jobKey))
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
