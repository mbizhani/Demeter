package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.User;

import java.util.List;

public interface IUserService {
	List<User> list();

	User getUser(String username);

	void saveOrUpdate(User user);
}
