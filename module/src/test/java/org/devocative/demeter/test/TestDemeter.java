package org.devocative.demeter.test;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.core.EStartupStep;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterWebApplication;
import org.devocative.demeter.web.dpage.LoginDPage;
import org.devocative.demeter.web.dpage.RoleFormDPage;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDemeter {
	private static WicketTester tester;
	private static ISecurityService securityService;

	private static String PROFILE = "hsqldb";

	// ------------------------------

	public static void setPROFILE(String PROFILE) {
		TestDemeter.PROFILE = PROFILE;
	}

	@BeforeClass
	public static void setUp() {
		System.setProperty(DemeterCore.CONFIG_PROFILE, PROFILE);

		DemeterCore.get().init();

		securityService = DemeterCore.get().getApplicationContext().getBean(ISecurityService.class);
		tester = new WicketTester(new DemeterWebApplication());

		// NOTE: the following config overwrites whatever is set in config_*.properties
		ConfigUtil.updateKey(DemeterConfigKey.UserDefaultLocale.getKey(), "fa");       //for d00CheckAccessDenied()
		ConfigUtil.updateKey(DemeterConfigKey.LoginCaptchaEnabled.getKey(), "false");  //for d01Login()
	}

	@AfterClass
	public static void shutdown() {
		DemeterCore.get().shutdown();
	}

	// --------------- B

	@Test
	public void b00IncompleteStartup() {
		if (!ConfigUtil.getBoolean("dmt.db.update.ddl", false)) {
			Assert.assertEquals("Check startup 'Database' step: ", EStartupStep.Database, DemeterCore.get().getLatestStat().getStep());
			Assert.assertTrue("Check database diff of applied scripts: ", DemeterCore.get().getDbDiffs().size() > 0);

			DemeterCore.get().applyAllDbDiffs();
			DemeterCore.get().resume();
		}
		Assert.assertEquals("Check startup 'End' step: ", EStartupStep.End, DemeterCore.get().getLatestStat().getStep());

		if (ConfigUtil.hasKey(DemeterConfigKey.StartupGroovyScript)) {
			Assert.assertEquals("Check StartupGroovyScript: ", "UP", System.getProperty("THE_LDAP"));
		}
	}

	@Test
	public void b01CheckAllEntities() throws ClassNotFoundException {
		IPersistorService persistorService = DemeterCore.get().getApplicationContext().getBean(IPersistorService.class);
		Assert.assertNotNull("Check IPersistorService existence: ", persistorService);

		List<String> entities = DemeterCore.get().getEntities();
		Assert.assertNotNull(entities);
		Assert.assertTrue("Check entities names existence: ", entities.size() > 0);

		System.out.println("--- TOTAL ENTITIES: " + entities.size());

		for (String entity : entities) {
			System.out.println("\t--- " + entity);
			List<?> list = persistorService.list(Class.forName(entity));
			Assert.assertNotNull("Check entities class existence: ", list);
		}
	}

	// --------------- D

	@Test
	public void d00CheckAccessDenied() {
		Assert.assertEquals("Default Locale: ", "fa", ConfigUtil.getString(DemeterConfigKey.UserDefaultLocale));
		tester.executeUrl("./dvc/dmt/users");
		Assert.assertTrue("Check login redirect: ", tester.getLastResponseAsString().contains("<title>ورود به سامانه</title>"));
	}

	@Test
	public void d01Login() {
		Assert.assertFalse("Check no-captcha config item: ", ConfigUtil.getBoolean(DemeterConfigKey.LoginCaptchaEnabled));

		Assert.assertEquals("Check current user as 'guest': ", "guest", securityService.getCurrentUser().getUsername());

		LoginDPage loginDPage = new LoginDPage("dPage", Collections.<String>emptyList());
		tester.startComponentInPage(loginDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("username", "root");
		form.setValue("password", "root");
		form.submit("signIn");
	}

	@Test
	public void d02CurrentUser() {
		Assert.assertEquals("Check current user as 'root': ", "root", securityService.getCurrentUser().getUsername());

		String roles = securityService.getCurrentUser().getRoles().toString();

		Assert.assertTrue("Check user 'root' has 'Root' role: ", roles.contains("Root"));
		Assert.assertTrue("Check user 'root' has 'Admin' role: ", roles.contains("Admin"));
		Assert.assertTrue("Check user 'root' has 'AuthByDB' role: ", roles.contains("AuthByDB"));
		Assert.assertTrue("Check user 'root' has 'User' role: ", roles.contains("User"));
	}

	@Test
	public void d03CheckAllPages() throws Exception {
		IDPageInstanceService pageInstanceService = DemeterCore.get().getApplicationContext().getBean(IDPageInstanceService.class);
		UserVO.PageVO defaultPages = pageInstanceService.getDefaultPages();
		Set<DPageInfo> pageInfoSet = new HashSet<>();

		for (String aUri : defaultPages.getAccessibleUri()) {
			DPageInstance dPageInstance = pageInstanceService.loadByUri(aUri);
			String uri = "./dvc" + dPageInstance.getUri();
			System.out.printf("\n\n >>>> uri = %s\n\n", uri);
			tester.executeUrl(uri);
			String responseAsString = tester.getLastResponseAsString();
			Assert.assertTrue("Check response content for uri '" + uri + "'", responseAsString.length() > 0);
			Assert.assertFalse("Check response content has title for uri '" + uri + "'", responseAsString.contains("<title></title>"));

			pageInfoSet.add(dPageInstance.getPageInfo());
		}

		for (DPageInfo pageInfo : pageInfoSet) {
			String type = pageInfo.getTypeAlt() != null ? pageInfo.getTypeAlt() : pageInfo.getType();
			Assert.assertNotNull("Check DPageInfo has type '" + type + "'", type);

			Class<?> dPageClass = Class.forName(type);
			Constructor<?> constructor = dPageClass.getDeclaredConstructor(String.class, List.class);
			DPage dPage = (DPage) constructor.newInstance("dPage", Collections.<String>emptyList());

			System.out.printf("\n\n >>>> DPage = %s\n\n", type);

			tester.startComponentInPage(dPage);
		}
	}

	@Test
	public void d04UniqueConstraintViolation() {
		IPersistorService persistorService = DemeterCore.get().getApplicationContext().getBean(IPersistorService.class);


		User user = new User();
		user.setUsername("root");
		user.setAuthMechanism(EAuthMechanism.LDAP);

		user.setPerson(new Person());

		IUserService userService = DemeterCore.get().getApplicationContext().getBean(IUserService.class);
		try {
			userService.saveOrUpdate(user); //Exception should be raised!
			Assert.assertEquals("DemeterException.DuplicateUsername should be thrown!", 1, 2);
		} catch (DemeterException e) {
			Assert.assertTrue("DemeterException.DuplicateUsername catched: ", e.getErrorCode().equals(DemeterErrorCode.DuplicateUsername));
		}

		Assert.assertEquals("Check total number of users: ", 3, userService.list().size());


		Role role = new Role();
		role.setName("User");

		IRoleService roleService = DemeterCore.get().getApplicationContext().getBean(IRoleService.class);
		try {
			roleService.saveOrUpdate(role); //Exception should be raised!
			Assert.assertTrue("DemeterException.DuplicateRoleName should be thrown!", false);
		} catch (DemeterException e) {
			Assert.assertTrue("DemeterException.DuplicateRoleName catched", e.getErrorCode().equals(DemeterErrorCode.DuplicateRoleName));
		}
		persistorService.endSession();

		Assert.assertEquals("Check total number of roles 1st", 7, roleService.list().size());

		// ---------------

		AtomicInteger aint = new AtomicInteger(0);
		UserVO currentUser = securityService.getCurrentUser();

		Role rMain = new Role();
		rMain.setName("TestDup");

		Thread th = new Thread(() -> {
			securityService.authenticate(currentUser);

			Role rTh = new Role();
			rTh.setName("TestDup");

			try {
				roleService.saveOrUpdate(rTh);
				aint.incrementAndGet();
			} catch (DemeterException e) {
				Assert.assertTrue("Check role name duplication prevention in thread", e.getErrorCode().equals(DemeterErrorCode.DuplicateRoleName));
			}

			persistorService.endSession();
		});
		th.start();

		try {
			roleService.saveOrUpdate(rMain);
			aint.incrementAndGet();
		} catch (DemeterException e) {
			Assert.assertTrue("Check role name duplication prevention", e.getErrorCode().equals(DemeterErrorCode.DuplicateRoleName));
		}
		persistorService.endSession();

		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals("Check new role name insertion once", 1, aint.get());
		Assert.assertEquals("Check total number of roles 2nd", 8, roleService.list().size()); // TOTAL ROLES = 8


		/*
		// Insert New Record
		Role merge = new Role();
		merge.setName("Merge");
		merge.setRowMode(ERowMod.NORMAL);
		persistorService.merge(merge);

		Role testDup = roleService.loadByName("TestDup");
		persistorService.endSession();

		// Update Record
		testDup.setName("123");
		persistorService.merge(testDup);
		persistorService.endSession();


		testDup.setName("qqq");
		persistorService.merge(testDup);
		persistorService.endSession();
		*/
	}

	// --------------- F

	@Test
	public void f01AddRoleByForm() {
		RoleFormDPage roleFormDPage = new RoleFormDPage("dPage");
		tester.startComponentInPage(roleFormDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("floatTable:name:textField", "role1");
		tester.executeAjaxEvent("dPage:form:save", "click");

		IRoleService roleService = DemeterCore.get().getApplicationContext().getBean(IRoleService.class);
		Assert.assertEquals("Check total number of roles 3rd", 9, roleService.list().size()); // TOTAL ROLES = 9
	}
}
