package org.devocative.demeter.web;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.devocative.demeter.entity.IPrivilegeKey;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.vo.UserVO;

import javax.inject.Inject;
import java.util.List;

public abstract class DPage extends Panel {
	private static final long serialVersionUID = -5981362081515131792L;

	@Inject
	private IDPageInstanceService pageInstanceService;

	// ------------------------------

	public DPage(String id, List<String> params) {
		super(id);
	}

	// ------------------------------

	protected void redirectToDPage(Class<? extends DPage> dPageClass) {
		throw new RedirectToUrlException(getUriForPage(dPageClass));
	}

	protected String getUriForPage(Class<? extends DPage> dPageClass) {
		String uriByPage = pageInstanceService.getUriByPage(dPageClass);
		if (uriByPage.length() > 0 && uriByPage.charAt(0) == '/') {
			uriByPage = uriByPage.substring(1);
		}
		return UrlUtils.rewriteToContextRelative(uriByPage, RequestCycle.get());
	}

	protected UserVO getCurrentUser() {
		return DemeterWebSession.get().getUserVO();
	}

	protected boolean hasPermission(IPrivilegeKey privilegeKey) {
		return getCurrentUser().hasPermission(privilegeKey);
	}

	protected boolean hasPermission(Class<? extends DPage> dPageClass) {
		return getCurrentUser().hasPermission(pageInstanceService.getUriByPage(dPageClass));
	}
}
