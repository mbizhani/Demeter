package org.devocative.demeter.web.http;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.iservice.IRequestLifecycle;
import org.devocative.demeter.web.DemeterWebParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebListener
public class DemeterWebListener implements ServletContextListener, ServletRequestListener {
	private static final Logger logger = LoggerFactory.getLogger(DemeterWebListener.class);

	private List<IRequestLifecycle> requestLifecycleBeans = new ArrayList<>();

	// ------------------------------

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("##========================");
		logger.info("##== Context Initialized!");
		DemeterCore.get().init();
		logger.info("##========================");

		boolean deployment = ConfigUtil.getBoolean(DemeterConfigKey.DeploymentMode);
		sce.getServletContext().setInitParameter("configuration", deployment ? "deployment" : "development");

		Map<String, IRequestLifecycle> beans = DemeterCore.get().getApplicationContext().getBeansOfType(IRequestLifecycle.class);
		requestLifecycleBeans.addAll(beans.values());
		logger.info("DemeterWebListener.RequestLifecycle: No Of Beans = [{}]", beans.size());

		sce.getServletContext().setAttribute(DemeterWebParam.DEMETER_APP_CTX, DemeterCore.get().getApplicationContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("##== Context Destroyed!");
		DemeterCore.get().shutdown();
		logger.info("##========================");
	}

	// ---------------

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		logger.debug("DemeterWebListener.requestInitialized");

		for (IRequestLifecycle requestLifecycle : requestLifecycleBeans) {
			try {
				requestLifecycle.beforeRequest();
			} catch (Exception e) {
				logger.error("DemeterWebListener.requestLifecycle: bean={}",
					requestLifecycle.getClass().getName(), e);
			}
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		logger.debug("DemeterWebListener.requestDestroyed");

		for (IRequestLifecycle requestLifecycle : requestLifecycleBeans) {
			try {
				requestLifecycle.afterResponse();
			} catch (Exception e) {
				logger.error("DemeterWebListener.requestDestroyed: bean={}",
					requestLifecycle.getClass().getName(), e);
			}
		}
	}
}
