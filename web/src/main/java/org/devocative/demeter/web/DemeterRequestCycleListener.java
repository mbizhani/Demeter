package org.devocative.demeter.web;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.IRequestLifecycle;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class DemeterRequestCycleListener extends AbstractRequestCycleListener {
	private static Logger logger = LoggerFactory.getLogger(DemeterRequestCycleListener.class);

	private Map<String, IRequestLifecycle> requestLifecycleBeans;
	private ISecurityService securityService;

	public DemeterRequestCycleListener() {
		requestLifecycleBeans = ModuleLoader.getApplicationContext().getBeansOfType(IRequestLifecycle.class);
		for (String beanName : requestLifecycleBeans.keySet()) {
			logger.info("IRequestLifecycle bean: {}", beanName);
		}

		securityService = ModuleLoader.getApplicationContext().getBean(ISecurityService.class);
	}

	@Override
	public void onBeginRequest(RequestCycle cycle) {
		if (requestLifecycleBeans != null) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
				requestLifecycle.beforeRequest();
			}
		}

		UserVO currentUser = DemeterWebSession.get().getUserVO();
		securityService.authenticate(currentUser);
	}

	@Override
	public void onEndRequest(RequestCycle cycle) {
		if (requestLifecycleBeans != null) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans.values()) {
				requestLifecycle.afterResponse();
			}
		}

		UserVO currentUser = securityService.getCurrentUser();
		DemeterWebSession.get().setUserVO(currentUser);
	}

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		//TODO an Exception Page
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
