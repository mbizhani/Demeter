//overwrite
package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WNumberInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class UserFormDPage extends DPage {
	private static final long serialVersionUID = 1943901057L;

	@Inject
	private IUserService userService;

	private User entity;

	// ------------------------------

	public UserFormDPage(String id) {
		this(id, new User());
	}

	// Main Constructor - For Ajax Call
	public UserFormDPage(String id, User entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public UserFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			userService.load(Long.valueOf(params.get(0))) :
			new User();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("username")
			.setRequired(true)
			.setLabel(new ResourceModel("User.username")));
		floatTable.add(new WTextInput("password")
			.setLabel(new ResourceModel("User.password")));
		floatTable.add(new WSelectionInput("authMechanism", EAuthMechanism.list(), false)
			.setLabel(new ResourceModel("User.authMechanism")));
		floatTable.add(new WSelectionInput("status", EUserStatus.list(), false)
			.setLabel(new ResourceModel("User.status")));
		floatTable.add(new WSelectionInput("locale", ELocale.list(), false)
			.setLabel(new ResourceModel("User.locale")));
		floatTable.add(new WSelectionInput("calendarType", ECalendar.list(), false)
			.setLabel(new ResourceModel("User.calendarType")));
		floatTable.add(new WSelectionInput("layoutDirectionType", ELayoutDirection.list(), false)
			.setLabel(new ResourceModel("User.layoutDirectionType")));
		floatTable.add(new WSelectionInput("datePatternType", EDatePatternType.list(), false)
			.setLabel(new ResourceModel("User.datePatternType")));
		floatTable.add(new WSelectionInput("dateTimePatternType", EDateTimePatternType.list(), false)
			.setLabel(new ResourceModel("User.dateTimePatternType")));
		floatTable.add(new WBooleanInput("admin")
			.setRequired(true)
			.setLabel(new ResourceModel("User.admin")));
		floatTable.add(new WNumberInput("sessionTimeout", Integer.class)
			.setRequired(true)
			.setLabel(new ResourceModel("User.sessionTimeout")));
		floatTable.add(new WSelectionInput("roles", userService.getRolesList(), true)
			.setLabel(new ResourceModel("User.roles")));
		floatTable.add(new WSelectionInput("authorizations", userService.getAuthorizationsList(), true)
			.setLabel(new ResourceModel("User.authorizations")));

		Form<User> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), DemeterIcon.SAVE) {
			private static final long serialVersionUID = -162851351L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				userService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(UserFormDPage.this, target)) {
					UrlUtil.redirectTo(UserListDPage.class);
				}
			}
		});
		add(form);
	}
}