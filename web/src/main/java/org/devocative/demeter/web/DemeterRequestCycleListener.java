package org.devocative.demeter.web;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.devocative.demeter.iservice.IRequestLifecycle;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

public class DemeterRequestCycleListener extends AbstractRequestCycleListener {
	private static final Logger logger = LoggerFactory.getLogger(DemeterRequestCycleListener.class);

	private final List<IRequestLifecycle> requestLifecycleBeans;
	private final ISecurityService securityService;

	// ------------------------------

	public DemeterRequestCycleListener(List<IRequestLifecycle> requestLifecycleBeans, ISecurityService securityService) {
		this.requestLifecycleBeans = requestLifecycleBeans;
		this.securityService = securityService;
	}

	// ------------------------------

	@Override
	public void onBeginRequest(RequestCycle cycle) {
		boolean isWSRq = WebUtil.isWebSocketRequest(cycle);
		logger.debug("DemeterRequestCycleListener.onBeginRequest: IsWSRq={}", isWSRq);

		try {
			UserVO currentUser = DemeterWebSession.get().getUserVO();
			if (currentUser == null) {
				currentUser = securityService.getGuestUser();
			}
			DemeterWebSession.get().setUserVO(currentUser);
			securityService.authenticate(currentUser);
		} catch (Exception e) {
			logger.error("DemeterRequestCycleListener.onBeginRequest: setting currentUser", e);
		}

		// Other request cycles are handled in DemeterWebListener, only WebSocket is handled here!
		if (isWSRq) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans) {
				try {
					requestLifecycle.beforeRequest();
				} catch (Exception e) {
					logger.error("IRequestLifecycle.beforeRequest(): bean = {}",
						requestLifecycle.getClass().getName(), e);
				}
			}
		}
	}

	@Override
	public void onEndRequest(RequestCycle cycle) {
		boolean isWSRs = WebUtil.isWebSocketResponse(cycle);
		logger.debug("DemeterRequestCycleListener.onEndRequest: IsWSRs={}", isWSRs);

		try {
			UserVO currentUser = securityService.getCurrentUser();
			DemeterWebSession.get().setUserVO(currentUser);
			setSessionTimeout(cycle, currentUser.getSessionTimeout());
		} catch (Exception e) {
			logger.error("DemeterRequestCycleListener.onEndRequest: removing currentUser", e);
		}

		// Other request cycles are handled in DemeterWebListener, only WebSocket is handled here!
		if (isWSRs) {
			for (IRequestLifecycle requestLifecycle : requestLifecycleBeans) {
				try {
					requestLifecycle.afterResponse();
				} catch (Exception e) {
					logger.error("IRequestLifecycle.afterResponse(): bean = {}",
						requestLifecycle.getClass().getName(), e);
				}
			}
		}
	}

	@Override
	public IRequestHandler onException(RequestCycle cycle, Exception ex) {
		//TODO an Exception Page
		return super.onException(cycle, ex);
	}

	// ------------------------------

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
