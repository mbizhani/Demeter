package org.devocative.demeter.web;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.IRequestLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class DemeterRequestCycleListener extends AbstractRequestCycleListener {
	private static Logger logger = LoggerFactory.getLogger(DemeterRequestCycleListener.class);

	Map<String, IRequestLifecycle> requestLifecycleBeans;

	public DemeterRequestCycleListener() {
		requestLifecycleBeans = ModuleLoader.getApplicationContext().getBeansOfType(IRequestLifecycle.class);
		for (String beanName : requestLifecycleBeans.keySet()) {
			logger.info("IRequestLifecycle bean: {}", beanName);
		}
	}

	@Override
	public void onBeginRequest(RequestCycle cycle) {
		if (requestLifecycleBeans != null) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
				requestLifecycle.beforeRequest();
			}
		}

		//TODO current user from session to service
	}

	@Override
	public void onEndRequest(RequestCycle cycle) {
		if (requestLifecycleBeans != null) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
				requestLifecycle.afterResponse();
			}
		}

		//TODO current user from service to session
	}

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		return super.onException(cycle, ex);
	}

	private void setSessionTimeout(RequestCycle cycle, int timeout) {
		Object containerRequest = cycle.getRequest().getContainerRequest();
		if (containerRequest instanceof HttpServletRequest) {
			HttpServletRequest hsr = (HttpServletRequest) containerRequest;
			HttpSession session = hsr.getSession();
			session.setMaxInactiveInterval(timeout);
		}
	}
}
