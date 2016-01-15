package org.devocative.demeter.core;

import org.devocative.demeter.entity.Person;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.junit.Test;

import java.util.List;

public class TestCore {
	@Test
	public void testModuleLoader() {
		ModuleLoader.init();

		Person person = new Person();
		person.setFirstName("A");
		person.setLastName("B");

		IPersistorService service = ModuleLoader.getApplicationContext().getBean(IPersistorService.class);

		service.saveOrUpdate(person);
		service.commitOrRollback();

		List<Person> list = service.list(Person.class);
		for (Person p : list) {
			System.out.println(p);
		}

		ModuleLoader.shutdown();
	}
}
