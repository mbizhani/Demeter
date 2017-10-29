package org.devocative.demeter.web;

import org.devocative.demeter.entity.IPrivilegeKey;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.WPanel;

import javax.inject.Inject;

public class DPanel extends WPanel {
	private static final long serialVersionUID = -6447704759510772487L;

	@Inject
	private ISecurityService securityService;

	// ------------------------------

	public DPanel(String id) {
		super(id);
	}

	// ------------------------------

	protected UserVO getCurrentUser() {
		return securityService.getCurrentUser();
	}

	protected boolean hasPermission(IPrivilegeKey privilegeKey) {
		return getCurrentUser().hasPermission(privilegeKey);
	}
}
