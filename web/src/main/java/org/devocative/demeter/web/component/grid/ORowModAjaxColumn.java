package org.devocative.demeter.web.component.grid;

import org.apache.wicket.model.IModel;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.ICreatorUser;
import org.devocative.demeter.entity.IRowMod;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.HTMLBase;

public abstract class ORowModAjaxColumn<T> extends OAjaxLinkColumn<T> {
	private static final long serialVersionUID = -1722174140462799957L;

	private UserVO currentUser;

	public ORowModAjaxColumn(IModel<String> text, HTMLBase linkContent) {
		super(text, linkContent);

		currentUser = DemeterCore.getApplicationContext().getBean(ISecurityService.class).getCurrentUser();
	}

	@Override
	public boolean onCellRender(T bean, String id) {
		if (bean instanceof IRowMod) {
			IRowMod rowMod = (IRowMod) bean;

			ICreatorUser creatorUser = null;
			if (bean instanceof ICreatorUser) {
				creatorUser = (ICreatorUser) bean;
			}

			return
				ERowMod.NORMAL.equals(rowMod.getRowMod()) ||
					(ERowMod.ROOT.equals(rowMod.getRowMod()) && currentUser.isRoot()) ||
					(ERowMod.ADMIN.equals(rowMod.getRowMod()) && currentUser.isAdmin()) ||
					(creatorUser != null &&
						ERowMod.CREATOR.equals(rowMod.getRowMod()) &&
						currentUser.getUserId().equals(creatorUser.getCreatorUserId()));
		}
		return super.onCellRender(bean, id);
	}
}
