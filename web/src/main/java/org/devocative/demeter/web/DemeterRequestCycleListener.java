package org.devocative.demeter.web;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class DemeterRequestCycleListener extends AbstractRequestCycleListener {
	private static Logger logger = LoggerFactory.getLogger(DemeterRequestCycleListener.class);

	private ISecurityService securityService;

	public DemeterRequestCycleListener() {
		securityService = ModuleLoader.getApplicationContext().getBean(ISecurityService.class);
	}

	@Override
	public void onBeginRequest(RequestCycle cycle) {
		logger.debug("DemeterRequestCycleListener.onBeginRequest");

		UserVO currentUser = DemeterWebSession.get().getUserVO();
		securityService.authenticate(currentUser);
	}

	@Override
	public void onEndRequest(RequestCycle cycle) {
		logger.debug("DemeterRequestCycleListener.onEndRequest");

		UserVO currentUser = securityService.getCurrentUser();
		DemeterWebSession.get().setUserVO(currentUser);
		setSessionTimeout(cycle, currentUser.getSessionTimeout());
	}

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		//TODO an Exception Page
		return super.onException(cycle, ex);
	}

	private void setSessionTimeout(RequestCycle cycle, int timeoutInMinutes) {
		Object containerRequest = cycle.getRequest().getContainerRequest();
		if (containerRequest instanceof HttpServletRequest) {
			HttpServletRequest hsr = (HttpServletRequest) containerRequest;
			HttpSession session = hsr.getSession();
			if (timeoutInMinutes > 0) {
				session.setMaxInactiveInterval(timeoutInMinutes * 60);
			} else {
				session.setMaxInactiveInterval(-1);
			}
		}
	}
}
