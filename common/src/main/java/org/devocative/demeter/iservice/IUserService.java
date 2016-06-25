package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.UserVO;

import java.util.List;

public interface IUserService {
	List<User> list();

	User loadByUsername(String username);

	void saveOrUpdate(User user);

	UserVO createOrUpdateUser(String username, String password, String firstName, String lastName);

	UserVO loadVOByUsername(String username);
}
