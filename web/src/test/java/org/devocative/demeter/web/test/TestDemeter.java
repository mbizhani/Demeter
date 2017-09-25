package org.devocative.demeter.web.test;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterWebApplication;
import org.devocative.demeter.web.dpage.LoginDPage;
import org.devocative.demeter.web.dpage.RoleFormDPage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDemeter {
	private static WicketTester tester;

	private static ISecurityService securityService;

	@BeforeClass
	public static void setUp() {
		DemeterCore.init();
		securityService = DemeterCore.getApplicationContext().getBean(ISecurityService.class);
		tester = new WicketTester(new DemeterWebApplication());

		ConfigUtil.updateKey(DemeterConfigKey.LoginCaptchaEnabled.getKey(), "false");
	}

	@Test
	public void a00CheckAccessDenied() {
		tester.executeUrl("./dvc/dmt/users");
		Assert.assertTrue(tester.getLastResponseAsString().contains("<title>ورود به سامانه</title>"));
	}

	@Test
	public void a01Login() {
		LoginDPage loginDPage = new LoginDPage("dPage", Collections.<String>emptyList());
		tester.startComponentInPage(loginDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("username", "root");
		form.setValue("password", "root");
		form.submit("signIn");
	}

	@Test
	public void a02CurrentUser() {
		Assert.assertEquals("root", securityService.getCurrentUser().getUsername());

		String roles = securityService.getCurrentUser().getRoles().toString();

		Assert.assertTrue(roles.contains("Root"));
		Assert.assertTrue(roles.contains("User"));
		Assert.assertTrue(roles.contains("AuthByDB"));
	}

	@Test
	public void a02CheckAllPages() throws Exception {
		IDPageInstanceService pageInstanceService = DemeterCore.getApplicationContext().getBean(IDPageInstanceService.class);
		UserVO.PageVO defaultPages = pageInstanceService.getDefaultPages();
		Set<DPageInfo> pageInfoSet = new HashSet<>();

		for (String aUri : defaultPages.getAccessibleUri()) {
			DPageInstance dPageInstance = pageInstanceService.loadByUri(aUri);
			String uri = "./dvc" + dPageInstance.getUri();
			System.out.printf("\n\n >>>> uri = %s\n\n", uri);
			tester.executeUrl(uri);
			String responseAsString = tester.getLastResponseAsString();
			Assert.assertTrue(responseAsString.length() > 0);
			Assert.assertFalse(responseAsString.contains("<title></title>"));

			pageInfoSet.add(dPageInstance.getPageInfo());
		}

		for (DPageInfo pageInfo : pageInfoSet) {
			String type = pageInfo.getTypeAlt() != null ? pageInfo.getTypeAlt() : pageInfo.getType();
			Assert.assertNotNull(type);

			Class<?> dPageClass = Class.forName(type);
			Constructor<?> constructor = dPageClass.getDeclaredConstructor(String.class, List.class);
			DPage dPage = (DPage) constructor.newInstance("dPage", Collections.<String>emptyList());

			System.out.printf("\n\n >>>> DPage = %s\n\n", type);

			tester.startComponentInPage(dPage);
		}
	}

	@Test
	public void s01AddRole() {
		RoleFormDPage roleFormDPage = new RoleFormDPage("dPage");
		tester.startComponentInPage(roleFormDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("floatTable:name", "role1");
		form.submit("save");
	}
}
