package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.User;

public interface IUserService {
	User getUser(String username);

	void saveOrUpdate(User user);
}
