//overwrite
package org.devocative.demeter.service;

import org.devocative.demeter.entity.Person;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IPersonService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.filter.PersonFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dmtPersonService")
public class PersonService implements IPersonService {
	private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(Person entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public Person load(Long id) {
		return persistorService.get(Person.class, id);
	}

	@Override
	public List<Person> list() {
		return persistorService.list(Person.class);
	}

	@Override
	public List<Person> search(PersonFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(PersonFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(Person.class, "ent")
			.applyFilter(Person.class, "ent", filter)
			.object();
	}

	@Override
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getModifierUserList() {
		return persistorService.list(User.class);
	}

	// ==============================
}