package org.devocative.demeter.service;

import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.entity.DTaskSchedule;
import org.devocative.demeter.iservice.DTask;
import org.devocative.demeter.iservice.ITaskService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTaskScheduleJob implements Job {
	private static Logger logger = LoggerFactory.getLogger(DTaskScheduleJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String scheduleId = context.getJobDetail().getKey().getName();
		logger.info("DemeterSimpleTaskJob: scheduleId={}", scheduleId);

		IPersistorService persistorService = (IPersistorService) ModuleLoader.getApplicationContext().getBean("dmtPersistorService");
		ITaskService taskService = ModuleLoader.getApplicationContext().getBean(ITaskService.class);

		try {
			DTaskSchedule schedule = persistorService.get(DTaskSchedule.class, new Long(scheduleId));
			if (schedule.getEnabled() && schedule.getTask().getEnabled()) {
				Class<? extends DTask> taskClass = (Class<? extends DTask>) Class.forName(schedule.getTask().getType());
				taskService.start(taskClass, schedule.getRefId());
			}
		} catch (Exception e) {
			logger.error("DemeterSimpleTaskJob: schedule=" + scheduleId, e);
		} finally {
			persistorService.endSession();
		}

	}
}
