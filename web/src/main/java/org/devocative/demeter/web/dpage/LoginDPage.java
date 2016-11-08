package org.devocative.demeter.web.dpage;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterWebSession;
import org.devocative.demeter.web.Index;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DButton;

import javax.inject.Inject;
import java.util.List;

public class LoginDPage extends DPage {
	private static final long serialVersionUID = 9081154949752445017L;

	private String username, password;

	@Inject
	private ISecurityService securityService;

	public LoginDPage(String id, List<String> params) {
		super(id, params);

		Form form = new Form("form");
		form.setEnabled(ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity));
		add(form);

		form.add(new TextField<>("username", new PropertyModel<String>(this, "username"))
			.setLabel(new ResourceModel("User.username"))
			.setRequired(true));
		form.add(new PasswordTextField("password", new PropertyModel<String>(this, "password"))
			.setLabel(new ResourceModel("User.password"))
			.setRequired(true));

		form.add(new DButton("signIn") {
			private static final long serialVersionUID = 2122837596660815329L;

			@Override
			public void onFormSubmit() {
				securityService.authenticate(username, password);

				Class<? extends DPage> originalDPage = DemeterWebSession.get().getOriginalDPage();
				List<String> params = DemeterWebSession.get().getOriginalParams();
				DemeterWebSession.get().removeOriginal();

				if (originalDPage != null) {
					UrlUtil.redirectTo(originalDPage, params);
				} else {
					setResponsePage(Index.class);
				}
			}
		});
	}
}
