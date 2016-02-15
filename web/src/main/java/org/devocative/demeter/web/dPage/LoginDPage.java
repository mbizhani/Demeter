package org.devocative.demeter.web.dPage;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.Index;

import javax.inject.Inject;
import java.util.List;

public class LoginDPage extends DPage {
	private String username, password;

	@Inject
	private ISecurityService securityService;

	public LoginDPage(String id, List<String> params) {
		super(id, params);

		//TODO check params for authentication input from outside

		Form form = new Form("form");
		form.setEnabled(ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity));
		add(form);

		form.add(new FeedbackPanel("feedback"));

		form.add(new TextField<>("username", new PropertyModel<String>(this, "username"))
			.setLabel(new ResourceModel("User.username"))
			.setRequired(true));
		form.add(new PasswordTextField("password", new PropertyModel<String>(this, "password"))
			.setLabel(new ResourceModel("User.password"))
			.setRequired(true));

		// TODO if duplicated, develop WButton
		form.add(new Button("signIn") {
			@Override
			public void onSubmit() {
				try {
					securityService.authenticate(username, password);
					setResponsePage(Index.class);
				} catch (DemeterException e) {
					error(getString(e.getMessage()));
				} catch (Exception e) {
					error(e.getMessage());
				}
			}
		});
	}
}
