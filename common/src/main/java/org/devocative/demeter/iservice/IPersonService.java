//overwrite
package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.Person;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.filter.PersonFVO;

import java.util.List;

public interface IPersonService {
	void saveOrUpdate(Person entity);

	Person load(Long id);

	List<Person> list();

	List<Person> search(PersonFVO filter, long pageIndex, long pageSize);

	long count(PersonFVO filter);

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================
}