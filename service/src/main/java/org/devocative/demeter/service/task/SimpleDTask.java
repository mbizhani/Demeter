package org.devocative.demeter.service.task;

import org.devocative.demeter.iservice.DTask;
import org.devocative.demeter.iservice.ISecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SimpleDTask extends DTask {
	private static Logger logger = LoggerFactory.getLogger(SimpleDTask.class);

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
		logger.info("SimpleDTask.execute: {}", securityService.getCurrentUser());
	}
}
