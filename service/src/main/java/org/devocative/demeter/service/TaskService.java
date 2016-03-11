package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.core.xml.XDTask;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.entity.DTaskInfo;
import org.devocative.demeter.entity.DTaskSchedule;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.DTask;
import org.devocative.demeter.iservice.task.ITaskResultCallback;
import org.devocative.demeter.iservice.task.ITaskService;
import org.devocative.demeter.vo.UserVO;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service("dmtTaskService")
public class TaskService implements ITaskService, IApplicationLifecycle, RejectedExecutionHandler {
	private static Logger logger = LoggerFactory.getLogger(TaskService.class);

	private boolean enabled;
	private UserVO system;
	private Map<String, DTask> TASKS;
	private Scheduler scheduler;
	private ThreadPoolExecutor threadPoolExecutor;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private IPersistorService persistorService;

	// ------------ IApplicationLifecycle

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
			new LinkedBlockingQueue<Runnable>());

		threadPoolExecutor.setRejectedExecutionHandler(this);

		logger.info("TaskService.init(): ThreadPoolExecutor Up!");

		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			logger.info("TaskService.init(): Scheduler Up!");
		} catch (SchedulerException e) {
			logger.info("TaskService.init(): StdSchedulerFactory: ", e);
			throw new DSystemException("TaskService.init(): StdSchedulerFactory", e);
		}

		system = new UserVO();

		TASKS = new ConcurrentHashMap<>();

		persistorService
			.createQueryBuilder()
			.addSelect("update DTaskInfo ent set ent.enabled=false")
			.update();
		persistorService.commitOrRollback();

		Map<String, XModule> modules = ModuleLoader.getModules();
		for (XModule xModule : modules.values()) {
			if (xModule.getTasks() != null) {
				for (XDTask xdTask : xModule.getTasks()) {
					try {
						Class<?> beanType = Class.forName(xdTask.getType());
						ModuleLoader.getApplicationContext().getBean(beanType);
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
		return ApplicationLifecyclePriority.Medium;
	}

	// ------------- RejectedExecutionHandler

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
	}

	// ------------ ITaskService

	@Override
	public Future<?> start(Class<? extends DTask> taskClass) {
		return start(taskClass, null, null, null);
	}

	@Override
	public Future<?> start(Class<? extends DTask> taskClass, String id) {
		return start(taskClass, id, null, null);
	}

	@Override
	public Future<?> start(Class<? extends DTask> taskClass, String id, Object inputData, ITaskResultCallback resultCallback) {
		if (!enabled) {
			throw new DSystemException("Task handling is not enabled");
		}

		logger.info("Starting Task: class=[{}] - id=[{}] - inputData=[{}]", taskClass, id, inputData);

		DTask dTask = ModuleLoader.getApplicationContext().getBean(taskClass);
		dTask
			.setId(id)
			.setInputData(inputData)
			.setResultCallback(resultCallback);

		Future<?> result = null;
		if (TASKS.containsKey(dTask.getKey())) {
			logger.warn("Rerunning Task: {}", dTask.getKey());
		} else {
			TASKS.put(dTask.getKey(), dTask);
			result = threadPoolExecutor.submit(dTask);
			logger.info("Started Task: {}", dTask.getKey());
		}
		return result;
	}

	@Override
	public void stop(String key) {
	}

	@Override
	public void stopAll() {
	}

	// ------------ private

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

			DTaskSchedule schedule = new DTaskSchedule();
			schedule.setCronExpression(xdTask.getCronExpression());
			schedule.setTask(dTaskInfo);

			persistorService.saveOrUpdate(dTaskInfo);
			persistorService.saveOrUpdate(schedule);
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

	// ------------ Demeter ThreadPoolExecutor

	private class DemeterThreadPoolExecutor extends ThreadPoolExecutor {
		public DemeterThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
										 BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			securityService.authenticate(system);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			String key = DTask.getKEY();
			DTask dTask = TASKS.get(key);
			logger.info("Task finished: key=[{}], state=[{}], duration=[{}]", key, dTask.getState(), dTask.getDuration());
			TASKS.remove(key);
			persistorService.endSession();
		}
	}
}
