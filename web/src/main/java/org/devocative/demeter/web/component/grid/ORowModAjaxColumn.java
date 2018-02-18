package org.devocative.demeter.web.component.grid;

import org.apache.wicket.model.IModel;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.entity.ICreatorUser;
import org.devocative.demeter.entity.IRoleRowAccess;
import org.devocative.demeter.entity.IRowMode;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.HTMLBase;

import java.util.Collections;

public abstract class ORowModAjaxColumn<T> extends OAjaxLinkColumn<T> {
	private static final long serialVersionUID = -1722174140462799957L;

	private UserVO currentUser;

	public ORowModAjaxColumn(IModel<String> text, HTMLBase linkContent) {
		super(text, linkContent);

		currentUser = DemeterCore.get().getApplicationContext().getBean(ISecurityService.class).getCurrentUser();
	}

	@Override
	public boolean onCellRender(T bean, String id) {
		if (bean instanceof IRowMode) {
			IRowMode rowMod = (IRowMode) bean;

			ICreatorUser creatorUser = null;
			if (bean instanceof ICreatorUser) {
				creatorUser = (ICreatorUser) bean;
			}

			IRoleRowAccess roleRowAccess = null;
			if (bean instanceof IRoleRowAccess) {
				roleRowAccess = (IRoleRowAccess) bean;
			}

			switch (rowMod.getRowMod().getId()) {
				case ERowMode.NORMAL_ID:
					return true;

				case ERowMode.ROOT_ID:
					return currentUser.isRoot();

				case ERowMode.ADMIN_ID:
					return currentUser.isRoot() || currentUser.isAdmin();

				case ERowMode.ROLE_ID:
					if (roleRowAccess == null) {
						throw new RuntimeException("Invalid Row: row with rowMod=ROLE must implements IRoleRowAccess");
					}
					return currentUser.isRoot() || currentUser.isAdmin() ||
						!Collections.disjoint(currentUser.getRoles(), roleRowAccess.getAllowedRoles());

				case ERowMode.CREATOR_ID:
					if (creatorUser == null) {
						throw new RuntimeException("Invalid Row: row with rowMod=CREATOR must implements ICreatorUser");
					}
					return currentUser.isRoot() || currentUser.isAdmin() ||
						currentUser.getUserId().equals(creatorUser.getCreatorUserId());

				default:
					return currentUser.isRoot();

			}
		} else {
			return true;
		}
	}
}
