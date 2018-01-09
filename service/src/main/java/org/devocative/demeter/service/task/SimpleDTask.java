package org.devocative.demeter.service.task;

import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Scope("prototype")
@Component("dmtSimpleDTask")
public class SimpleDTask extends DTask<String> {
	private static Logger logger = LoggerFactory.getLogger(SimpleDTask.class);

	private AtomicBoolean cont = new AtomicBoolean(true);

	@Autowired
	private ISecurityService securityService;

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
		logger.info("SimpleDTask.execute: user={}", getCurrentUser());

		try {
			int idx = 1;
			int i = (int) (Math.random() * 10000);
			final int max = i + 30;
			for (; i <= max && cont.get(); i++) {
				Thread.sleep(((i % 3) + 1) * 1000);
				sendResult(String.format("counter = %02d | idx= %02d | user=%s", i, idx++, securityService.getCurrentUser()));
			}
			logger.info("SimpleDTask.executed: {}", getCurrentUser());
			sendResult("SimpleDTask.executed: " + getCurrentUser());
		} catch (InterruptedException e) {
			logger.warn("SimpleDTask Sleep Interrupted");
		}
	}

	@Override
	public void cancel() throws Exception {
		logger.info("SimpleDTask.cancel: {}", getCurrentUser());
		cont.set(false);
	}
}
