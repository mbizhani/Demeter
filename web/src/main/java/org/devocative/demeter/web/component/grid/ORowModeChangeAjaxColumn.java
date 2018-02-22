package org.devocative.demeter.web.component.grid;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.entity.IRowMode;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.DemeterWebSession;
import org.devocative.demeter.web.panel.RowModeChangePanel;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.window.WModalWindow;

public class ORowModeChangeAjaxColumn<T extends IRowMode> extends OAjaxLinkColumn<T> {
	private static final long serialVersionUID = -3783348687276724102L;

	private final WModalWindow modalWindow;

	// ------------------------------

	public ORowModeChangeAjaxColumn(WModalWindow modalWindow) {
		super(new Model<>(), DemeterIcon.SHIELD);

		this.modalWindow = modalWindow;
	}

	// ------------------------------

	@Override
	public void onClick(AjaxRequestTarget target, IModel<T> rowData) {
		modalWindow.setContent(new RowModeChangePanel(modalWindow.getContentId(), rowData.getObject()));
		modalWindow.show(target);
	}

	@Override
	public boolean onCellRender(T bean, String id) {
		UserVO currentUser = DemeterWebSession.get().getUserVO();
		return currentUser.isRoot() && !ERowMode.SYSTEM.equals(bean.getRowMode());
	}
}
