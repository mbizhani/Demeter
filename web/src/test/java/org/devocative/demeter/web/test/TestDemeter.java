package org.devocative.demeter.web.test;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.web.DemeterWebApplication;
import org.devocative.demeter.web.dpage.LoginDPage;
import org.devocative.demeter.web.dpage.RoleFormDPage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collections;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDemeter {
	private static WicketTester tester;

	private static ISecurityService securityService;

	@BeforeClass
	public static void setUp() {
		ModuleLoader.init();
		securityService = ModuleLoader.getApplicationContext().getBean(ISecurityService.class);
		tester = new WicketTester(new DemeterWebApplication());
	}

	@Test
	public void s00CheckAccessDenied() {
		tester.executeUrl("./dvc/dmt/users");
		Assert.assertTrue(tester.getLastResponseAsString().contains("<title>ورود به سامانه</title>"));
	}

	@Test
	public void s01Login() {
		LoginDPage loginDPage = new LoginDPage("dPage", Collections.<String>emptyList());
		tester.startComponentInPage(loginDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("username", "root");
		form.setValue("password", "root");
		form.submit("signIn");
	}

	@Test
	public void s02CurrentUser() {
		Assert.assertEquals("root", securityService.getCurrentUser().getUsername());

		String roles = securityService.getCurrentUser().getRoles().toString();

		Assert.assertTrue(roles.contains("Root"));
		Assert.assertTrue(roles.contains("User"));
		Assert.assertTrue(roles.contains("AuthByDB"));
	}

	@Test
	public void s03AddRole() {
		RoleFormDPage roleFormDPage = new RoleFormDPage("dPage");
		tester.startComponentInPage(roleFormDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("floatTable:name", "role1");
		form.submit("save");
	}
}
