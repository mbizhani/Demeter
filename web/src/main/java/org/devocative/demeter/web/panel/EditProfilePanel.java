package org.devocative.demeter.web.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.validator.WEqualInputValidator;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class EditProfilePanel extends DPanel {
	private static final long serialVersionUID = 2306498797301311526L;
	private static final Logger logger = LoggerFactory.getLogger(EditProfilePanel.class);

	@Inject
	private IUserService userService;

	private User entity;
	private WTextInput password, oldPassword;

	public EditProfilePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final UserVO currentUser = getCurrentUser();
		entity = userService.load(currentUser.getUserId());

		WebMarkupContainer userInfo = new WebMarkupContainer("userInfo", new CompoundPropertyModel<Object>(currentUser));
		add(userInfo);

		userInfo.add(new Label("username"));
		userInfo.add(new Label("authMechanism"));
		userInfo.add(new Label("roles"));
		userInfo.add(new Label("permissions"));
		userInfo.add(new Label("denials"));


		WFloatTable floatTable = new WFloatTable("floatTable");

		oldPassword = new WTextInput("oldPassword", new Model<String>(), true);
		oldPassword.setLabel(new ResourceModel("User.oldPassword"));
		oldPassword.setEnabled(EAuthMechanism.DATABASE.equals(currentUser.getAuthMechanism()));
		floatTable.add(oldPassword);

		password = new WTextInput("password", new Model<String>(), true);
		password.setLabel(new ResourceModel("User.password"));
		password.setEnabled(EAuthMechanism.DATABASE.equals(currentUser.getAuthMechanism()));
		floatTable.add(password);

		WTextInput password2 = new WTextInput("password2", new Model<String>(), true);
		password2.setLabel(new ResourceModel("User.password2"));
		password2.setEnabled(EAuthMechanism.DATABASE.equals(currentUser.getAuthMechanism()));
		floatTable.add(password2);

		floatTable.add(new WSelectionInput("locale", ELocale.list(), false)
			.setLabel(new ResourceModel("User.locale"))
			.setVisible(false) //TODO
		);
		floatTable.add(new WSelectionInput("calendarType", ECalendar.list(), false)
			.setLabel(new ResourceModel("User.calendarType"))
			.setVisible(false) //TODO
		);
		floatTable.add(new WSelectionInput("layoutDirectionType", ELayoutDirection.list(), false)
			.setLabel(new ResourceModel("User.layoutDirectionType"))
			.setVisible(false) //TODO
		);
		floatTable.add(new WSelectionInput("datePatternType", EDatePatternType.list(), false)
			.setLabel(new ResourceModel("User.datePatternType"))
			.setVisible(false) //TODO
		);
		floatTable.add(new WSelectionInput("dateTimePatternType", EDateTimePatternType.list(), false)
			.setLabel(new ResourceModel("User.dateTimePatternType"))
			.setVisible(false) //TODO
		);

		Form<User> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(new WEqualInputValidator(password, password2));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), DemeterIcon.SAVE) {
			private static final long serialVersionUID = -162851351L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				logger.info("User [{}] updated his profile.", entity.getUsername());

				userService.updateUser(entity, password.getModelObject(), oldPassword.getModelObject());

				WModalWindow.closeParentWindow(EditProfilePanel.this, target);
			}
		});
		add(form);

	}
}
