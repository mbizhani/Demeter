package org.devocative.demeter.web.component.grid;

import org.apache.wicket.model.IModel;
import org.devocative.demeter.entity.ICreatorUser;
import org.devocative.demeter.entity.IRoleRowAccess;
import org.devocative.demeter.entity.IRowMode;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.web.DemeterWebSession;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.HTMLBase;

import java.util.Collections;

/**
 * Since subclass {@link OEditAjaxColumn} must be used for all types, the {@code T} can not extends
 * {@link IRowMode} (in spite of {@link ORowModeChangeAjaxColumn})
 *
 * @param <T>
 */
public abstract class ORowModeAjaxColumn<T> extends OAjaxLinkColumn<T> {
	private static final long serialVersionUID = -1722174140462799957L;

	public ORowModeAjaxColumn(IModel<String> text, HTMLBase linkContent) {
		super(text, linkContent);
	}

	@Override
	public boolean onCellRender(T bean, String id) {
		if (bean instanceof IRowMode) {
			IRowMode rowMode = (IRowMode) bean;

			ICreatorUser creatorUser = null;
			if (bean instanceof ICreatorUser) {
				creatorUser = (ICreatorUser) bean;
			}

			IRoleRowAccess roleRowAccess = null;
			if (bean instanceof IRoleRowAccess) {
				roleRowAccess = (IRoleRowAccess) bean;
			}

			UserVO currentUser = DemeterWebSession.get().getUserVO();
			switch (rowMode.getRowMode()) {
				case SYSTEM:
					return false;

				case NORMAL:
					return true;

				case ROOT:
					return currentUser.isRoot();

				case ADMIN:
					return currentUser.isAdmin();

				case ROLE:
					if (roleRowAccess == null) {
						throw new RuntimeException("Invalid Row: row with rowMode=ROLE must implements IRoleRowAccess");
					}
					return currentUser.isAdmin() ||
						!Collections.disjoint(currentUser.getRoles(), roleRowAccess.getAllowedRoles());

				case CREATOR:
					if (creatorUser == null) {
						throw new RuntimeException("Invalid Row: row with rowMode=CREATOR must implements ICreatorUser");
					}
					return currentUser.isAdmin() || currentUser.getUserId().equals(creatorUser.getCreatorUserId());

				default:
					return currentUser.isRoot();

			}
		}

		return true;
	}
}
