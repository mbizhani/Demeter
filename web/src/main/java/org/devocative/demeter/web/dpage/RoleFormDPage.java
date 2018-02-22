package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.ERoleMode;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.validator.WAsciiIdentifierValidator;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RoleFormDPage extends DPage {
	private static final long serialVersionUID = 873729846L;

	@Inject
	private IRoleService roleService;

	private Role entity;

	// ------------------------------

	public RoleFormDPage(String id) {
		this(id, new Role());
	}

	// Main Constructor - For Ajax Call
	public RoleFormDPage(String id, Role entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public RoleFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			roleService.load(Long.valueOf(params.get(0))) :
			new Role();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setRequired(true)
			.setLabel(new ResourceModel("Role.name", "name"))
			.add(new  WAsciiIdentifierValidator())
		);
		floatTable.add(new WSelectionInput("roleMode", ERoleMode.list(), false)
			.setRequired(true)
			.setLabel(new ResourceModel("Role.roleMode", "roleMode")));
		floatTable.add(new WSelectionInput("permissions", roleService.getPermissionsList(), true)
			.setLabel(new ResourceModel("Role.permissions", "permissions")));
		floatTable.add(new WSelectionInput("denials", roleService.getDenialsList(), true)
			.setLabel(new ResourceModel("Role.denials", "denials")));

		Form<Role> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), DemeterIcon.SAVE) {
			private static final long serialVersionUID = -1404786146L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				roleService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(RoleFormDPage.this, target)) {
					UrlUtil.redirectTo(RoleListDPage.class);
				}
			}
		});
		add(form);
	}
}