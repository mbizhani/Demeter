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

		ConfigUtil.updateKey(DemeterConfigKey.LoginCaptchaEnabled.getKey(), "false");
	}

	@AfterClass
	public static void shutdown() {
		DemeterCore.get().shutdown();
	}

	// --------------- B

	@Test
	public void b00IncompleteStartup() {
		Assert.assertEquals(EStartupStep.Database, DemeterCore.get().getLatestStat().getStep());
		Assert.assertTrue(DemeterCore.get().getDbDiffs().size() > 0);

		DemeterCore.get().applyAllDbDiffs();
		DemeterCore.get().resume();
		Assert.assertEquals(EStartupStep.End, DemeterCore.get().getLatestStat().getStep());

		if (ConfigUtil.hasKey(DemeterConfigKey.StartupGroovyScript)) {
			Assert.assertEquals("UP", System.getProperty("THE_LDAP"));
		}
	}

	@Test
	public void b01CheckAllEntities() throws ClassNotFoundException {
		IPersistorService persistorService = DemeterCore.get().getApplicationContext().getBean(IPersistorService.class);
		Assert.assertNotNull(persistorService);

		List<String> entities = DemeterCore.get().getEntities();
		Assert.assertNotNull(entities);
		Assert.assertTrue(entities.size() > 0);

		System.out.println("--- TOTAL ENTITIES: " + entities.size());

		for (String entity : entities) {
			System.out.println("\t--- " + entity);
			List<?> list = persistorService.list(Class.forName(entity));
			Assert.assertNotNull(list);
		}
	}

	// --------------- D

	@Test
	public void d00CheckAccessDenied() {
		tester.executeUrl("./dvc/dmt/users");
		Assert.assertTrue(tester.getLastResponseAsString().contains("<title>ورود به سامانه</title>"));
	}

	@Test
	public void d01Login() {
		Assert.assertFalse(ConfigUtil.getBoolean(DemeterConfigKey.LoginCaptchaEnabled));

		LoginDPage loginDPage = new LoginDPage("dPage", Collections.<String>emptyList());
		tester.startComponentInPage(loginDPage);

		FormTester form = tester.newFormTester("dPage:form");
		form.setValue("username", "root");
		form.setValue("password", "root");
		form.submit("signIn");
	}

	@Test
	public void d02CurrentUser() {
		Assert.assertEquals("root", securityService.getCurrentUser().getUsername());

		String roles = securityService.getCurrentUser().getRoles().toString();

		Assert.assertTrue(roles.contains("Root"));
		Assert.assertTrue(roles.contains("User"));
		Assert.assertTrue(roles.contains("AuthByDB"));
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
			Assert.assertTrue(e.getErrorCode().equals(DemeterErrorCode.DuplicateUsername));
		}

		Assert.assertEquals(3, userService.list().size());


		Role role = new Role();
		role.setName("User");

		IRoleService roleService = DemeterCore.get().getApplicationContext().getBean(IRoleService.class);
		try {
			roleService.saveOrUpdate(role); //Exception should be raised!
			Assert.assertEquals("DemeterException.DuplicateRoleName should be thrown!", 1, 2);
		} catch (DemeterException e) {
			Assert.assertTrue(e.getErrorCode().equals(DemeterErrorCode.DuplicateRoleName));
		}
		persistorService.endSession();

		Assert.assertEquals(6, roleService.list().size());

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
				Assert.assertTrue(e.getErrorCode().equals(DemeterErrorCode.DuplicateRoleName));
			}

			persistorService.endSession();
		});
		th.start();

		try {
			roleService.saveOrUpdate(rMain);
			aint.incrementAndGet();
		} catch (DemeterException e) {
			Assert.assertTrue(e.getErrorCode().equals(DemeterErrorCode.DuplicateRoleName));
		}
		persistorService.endSession();

		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(1, aint.get());
		Assert.assertEquals(7, roleService.list().size()); // TOTAL ROLES = 7


		/*
		// Insert New Record
		Role merge = new Role();
		merge.setName("Merge");
		merge.setRowMod(ERowMod.NORMAL);
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
		Assert.assertEquals(8, roleService.list().size()); // TOTAL ROLES = 8
	}
}