package org.devocative.demeter.core;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.entity.Person;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.template.IStringTemplate;
import org.devocative.demeter.iservice.template.IStringTemplateService;
import org.devocative.demeter.iservice.template.TemplateEngineType;
import org.devocative.demeter.vo.filter.PersonFVO;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCore {
	private static Logger logger = LoggerFactory.getLogger(TestCore.class);

	private static IPersistorService persistorService;
	private static boolean skip = false;

	@BeforeClass
	public static void init() {
		try {
			ModuleLoader.init();

			persistorService = ModuleLoader.getApplicationContext().getBean(IPersistorService.class);
		} catch (Exception e) {
			logger.info("init failed");
			skip = true;
		}
	}

	@Test
	public void test01() {
		if (skip) {
			return;
		}

		Person person = new Person();
		person.setFirstName("John");
		person.setLastName("Blue");
		person.setBirthRegDate(new Date());

		persistorService.saveOrUpdate(person);
		persistorService.commitOrRollback();

		List<Person> list = persistorService.list(Person.class);
		for (Person p : list) {
			System.out.println(p);
		}
	}

	@Test
	public void test02() {
		if (skip) {
			return;
		}

		PersonFVO f1 = new PersonFVO()
			.setFirstName("Jo");
		int sizeF1 = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", f1)
			.list()
			.size();
		logger.info("sizeF1 = {}", sizeF1);
		Assert.assertTrue(sizeF1 > 0);

		PersonFVO f2 = new PersonFVO()
			.setLastName("Bl");
		int sizeF2 = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", f2)
			.list()
			.size();
		logger.info("sizeF2 = {}", sizeF2);
		Assert.assertTrue(sizeF2 == 0);

		PersonFVO f3 = new PersonFVO()
			.setHasUser(true);
		int sizeF3 = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", f3)
			.list()
			.size();
		logger.info("sizeF3 = {}", sizeF3);
		Assert.assertTrue(sizeF3 == 3); // root, system & guest users

		PersonFVO f4 = new PersonFVO()
			.setBirthRegDate(new RangeVO<>(null, new Date()));
		int sizeF4 = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", f4)
			.list()
			.size();
		logger.info("sizeF4 = {}", sizeF4);
		Assert.assertTrue(sizeF4 > 0);

		PersonFVO f5 = new PersonFVO()
			.setMyDate(new RangeVO<>(new Date(), null));
		int sizeF5 = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", f5)
			.list()
			.size();
		logger.info("sizeF5 = {}", sizeF5);
		Assert.assertTrue(sizeF5 == 0);

		try {
			PersonFVO f6 = new PersonFVO()
				.setSillyProp("Hi");
			persistorService
				.createQueryBuilder()
				.addSelect("select ent")
				.addFrom(Person.class, "ent")
				.applyFilter(Person.class, "ent", f6)
				.list()
				.size();
			Assert.assertTrue(false);
		} catch (Exception e) {
			logger.error("f6: Filter by 'sillyProp' = {}", e.getMessage());
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testStringTemplate() {
		IStringTemplateService templateService = ModuleLoader.getApplicationContext().getBean(IStringTemplateService.class);

		IStringTemplate stringTemplate = templateService.create(
			"int=${C_INT?c}&long=${C_Long?c}&bigDecimal=${BIG?c}&float=${FlT?c}&double=${dbl?c}&name=${f_name}",
			TemplateEngineType.FreeMarker
		);

		Map<String, Object> params = new HashMap<>();
		params.put("c_int", 12345);
		params.put("c_LONG", 999L);
		params.put("big", new BigDecimal(13600602));
		params.put("flt", 1.2);
		params.put("dbl", 55555.23633D);
		params.put("f_name", "Jack");

		String result = stringTemplate.process(params, false);
		System.out.println("result = " + result);

		Assert.assertEquals("int=12345&long=999&bigDecimal=13600602&float=1.2&double=55555.23633&name=Jack", result);

		stringTemplate = templateService.create(
			"int=${C_INT}&long=${C_Long}&bigDecimal=${BIG}&float=${FlT}&double=${dbl}&name=${f_name}",
			TemplateEngineType.FreeMarker
		);

		result = stringTemplate.process(params, true);
		System.out.println("result = " + result);

		Assert.assertEquals("int=12345&long=999&bigDecimal=13600602&float=1.2&double=55555.23633&name=Jack", result);
	}

	@AfterClass
	public static void shutdown() {
		if (skip) {
			return;
		}

		ModuleLoader.shutdown();
	}
}
