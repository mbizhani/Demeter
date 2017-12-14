package org.devocative.demeter.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.IRowMod;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.html.window.WModalWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class RowModChangePanel extends DPanel {
	private static final long serialVersionUID = -7301702757714281743L;
	private static final Logger logger = LoggerFactory.getLogger(RowModChangePanel.class);

	private IRowMod row;
	private ERowMod old;

	@Inject
	private IPersistorService persistorService;

	public RowModChangePanel(String id, IRowMod row) {
		super(id);

		this.row = row;
		this.old = row.getRowMod();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form form = new Form("form");
		form.add(new WSelectionInput("list", new PropertyModel<>(row, "rowMod"), ERowMod.accessList(), false));
		form.add(new DAjaxButton("update", DemeterIcon.SAVE) {
			private static final long serialVersionUID = -1528622888194462174L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				Object id = persistorService.updateFields(row, "rowMod");
				persistorService.commitOrRollback();

				logger.warn("RowModChangePanel: entity [{}] with id [{}] has changed from [{}] to [{}]",
					row.getClass().getName(), id, old, row.getRowMod());

				WModalWindow.closeParentWindow(RowModChangePanel.this, target);
			}
		});
		add(form);
	}
}
