//overwrite
package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class DPageInstanceFormDPage extends DPage {
	private static final long serialVersionUID = 1222973706L;

	@Inject
	private IDPageInstanceService dPageInstanceService;

	private DPageInstance entity;

	// ------------------------------

	public DPageInstanceFormDPage(String id) {
		this(id, new DPageInstance());
	}

	// Main Constructor - For Ajax Call
	public DPageInstanceFormDPage(String id, DPageInstance entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public DPageInstanceFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			dPageInstanceService.load(Long.valueOf(params.get(0))) :
			new DPageInstance();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("title")
			.setRequired(true)
			.setLabel(new ResourceModel("DPageInstance.title")));
		floatTable.add(new WTextInput("uri")
			.setRequired(true)
			.setLabel(new ResourceModel("DPageInstance.uri")));
		floatTable.add(new WBooleanInput("inMenu")
			.setRequired(true)
			.setLabel(new ResourceModel("DPageInstance.inMenu")));
		floatTable.add(new WTextInput("refId")
			.setLabel(new ResourceModel("DPageInstance.refId")));
		floatTable.add(new WSelectionInput("pageInfo", dPageInstanceService.getPageInfoList(), false)
			.setLabel(new ResourceModel("DPageInstance.pageInfo")));
		floatTable.add(new WSelectionInput("roles", dPageInstanceService.getRolesList(), true)
			.setLabel(new ResourceModel("DPageInstance.roles")));

		Form<DPageInstance> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), DemeterIcon.SAVE) {
			private static final long serialVersionUID = 793875954L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				dPageInstanceService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(DPageInstanceFormDPage.this, target)) {
					UrlUtil.redirectTo(DPageInstanceListDPage.class);
				}
			}
		});
		add(form);
	}
}