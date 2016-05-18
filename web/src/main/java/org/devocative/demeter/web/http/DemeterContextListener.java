package org.devocative.demeter.web.http;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.ModuleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DemeterContextListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(DemeterContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("##========================");
		logger.info("##== Context Initialized!");
		ModuleLoader.init();
		logger.info("##========================");

		boolean deployment = ConfigUtil.getBoolean(DemeterConfigKey.DeploymentMode);
		sce.getServletContext().setInitParameter("configuration", deployment ? "deployment" : "development");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("##== Context Destroyed!");
		ModuleLoader.shutdown();
		logger.info("##========================");
	}
}