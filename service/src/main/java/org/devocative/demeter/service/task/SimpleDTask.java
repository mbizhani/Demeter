package org.devocative.demeter.service.task;

import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("dmtSimpleDTask")
public class SimpleDTask extends DTask {
	private static Logger logger = LoggerFactory.getLogger(SimpleDTask.class);

	private Thread currentTh;

	@Override
	public void init() {
		logger.info("SimpleDTask.init");
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() {
		currentTh = Thread.currentThread();

		logger.info("SimpleDTask.execute: user={}, curTh={}", getCurrentUser(), currentTh);
		try {
			Thread.sleep(60000);
			logger.info("SimpleDTask.executed: {}", getCurrentUser());
		} catch (InterruptedException e) {
			logger.warn("SimpleDTask Sleep Interrupted");
		}
	}

	@Override
	public void cancel() throws Exception {
		logger.info("SimpleDTask.cancel: {}", getCurrentUser());
		currentTh.interrupt();
	}
}
