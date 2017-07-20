package org.devocative.demeter.web.http;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.iservice.IRequestLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import java.util.Map;

@WebListener
public class DemeterWebListener implements ServletContextListener, ServletRequestListener {
	private static final Logger logger = LoggerFactory.getLogger(DemeterWebListener.class);

	private Map<String, IRequestLifecycle> requestLifecycleBeans;

	// ------------------------------

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("##========================");
		logger.info("##== Context Initialized!");
		DemeterCore.init();
		logger.info("##========================");

		boolean deployment = ConfigUtil.getBoolean(DemeterConfigKey.DeploymentMode);
		sce.getServletContext().setInitParameter("configuration", deployment ? "deployment" : "development");

		requestLifecycleBeans = DemeterCore.getApplicationContext().getBeansOfType(IRequestLifecycle.class);
		for (String beanName : requestLifecycleBeans.keySet()) {
			logger.info("DemeterWebListener: IRequestLifecycle Bean = {}", beanName);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("##== Context Destroyed!");
		DemeterCore.shutdown();
		logger.info("##========================");
	}

	// ---------------

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		logger.debug("DemeterWebListener.requestInitialized");

		if (requestLifecycleBeans != null) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
				try {
					requestLifecycle.beforeRequest();
				} catch (Exception e) {
					logger.error("DemeterWebListener.requestLifecycle: bean={}",
						requestLifecycle.getClass().getName(), e);
				}
			}
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		logger.debug("DemeterWebListener.requestDestroyed");

		if (requestLifecycleBeans != null) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
				try {
					requestLifecycle.afterResponse();
				} catch (Exception e) {
					logger.error("DemeterWebListener.requestDestroyed: bean={}",
						requestLifecycle.getClass().getName(), e);
				}
			}
		}
	}
}
