package org.devocative.demeter.service;

import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.entity.DTaskSchedule;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.task.ITaskService;
import org.devocative.demeter.vo.UserVO;
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
		ISecurityService securityService = ModuleLoader.getApplicationContext().getBean(ISecurityService.class);
		UserVO currentUser = securityService.getCurrentUser();
		if (currentUser == null) {
			securityService.authenticate(securityService.getSystemUser());
		}

		try {
			DTaskSchedule schedule = persistorService.get(DTaskSchedule.class, new Long(scheduleId));
			if (schedule.getEnabled()) {
				taskService.start(schedule.getTask().getId(), null, schedule.getRefId(), null);
			}
		} catch (Exception e) {
			logger.error("DemeterSimpleTaskJob: schedule=" + scheduleId, e);
		} finally {
			persistorService.endSession();
		}
	}
}
