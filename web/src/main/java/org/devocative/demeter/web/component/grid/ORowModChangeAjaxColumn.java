package org.devocative.demeter.web.component.grid;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.demeter.entity.IRowMod;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.panel.RowModChangePanel;
import org.devocative.wickomp.html.window.WModalWindow;

public class ORowModChangeAjaxColumn<T extends IRowMod> extends ORowModAjaxColumn<T> {
	private static final long serialVersionUID = -3783348687276724102L;

	private final WModalWindow modalWindow;

	public ORowModChangeAjaxColumn(WModalWindow modalWindow) {
		super(new Model<>(), DemeterIcon.SHIELD);

		this.modalWindow = modalWindow;
	}

	@Override
	public void onClick(AjaxRequestTarget target, IModel<T> rowData) {
		modalWindow.setContent(new RowModChangePanel(modalWindow.getContentId(), rowData.getObject()));
		modalWindow.show(target);
	}
}
