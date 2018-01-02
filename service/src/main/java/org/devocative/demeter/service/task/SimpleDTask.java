package org.devocative.demeter.service.task;

import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Scope("prototype")
@Component("dmtSimpleDTask")
public class SimpleDTask extends DTask {
	private static Logger logger = LoggerFactory.getLogger(SimpleDTask.class);

	private AtomicBoolean cont = new AtomicBoolean(true);

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
			int i = (int) (Math.random() * 10000);
			final int max = i + 60;
			for (; i <= max && cont.get(); i++) {
				Thread.sleep(((i % 3) + 1) * 2000);
				sendResult(String.format("counter = %02d", i));
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
