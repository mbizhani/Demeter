package org.devocative.demeter.service.task;

import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.task.DTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Scope("prototype")
@Component("dmtSimpleDTask")
public class SimpleDTask extends DTask<String> {
	private static final Logger logger = LoggerFactory.getLogger(SimpleDTask.class);

	private AtomicBoolean cont = new AtomicBoolean(true);
	private int MAX;

	@Autowired
	private ISecurityService securityService;

	@Override
	public void init() {
		logger.info("SimpleDTask.init");

		MAX = new Random().nextInt(100) + 1;
		setDetail(String.valueOf(MAX));
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void execute() throws Exception {
		logger.info("SimpleDTask.execute: user={}", getCurrentUser());

		Thread th = new Thread(() -> {
			try {

				for (int i = 1; i <= MAX && cont.get(); i++) {
					logger.debug("SimpleDTask: i = {}", i);

					Thread.sleep(1000);

					sendResult(String.format("counter = %02d | user=%s", i, securityService.getCurrentUser()));
				}

				logger.info("SimpleDTask.executed: {}", getCurrentUser());

				sendResult("SimpleDTask.executed: " + getCurrentUser());
			} catch (InterruptedException e) {
				logger.warn("SimpleDTask Sleep Interrupted");
			}
		});
		th.start();
		th.join();
	}

	@Override
	public void cancel() {
		logger.info("SimpleDTask.cancel: {}", getCurrentUser());
		cont.set(false);
	}
}
