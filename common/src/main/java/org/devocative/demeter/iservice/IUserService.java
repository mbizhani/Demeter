package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.UserInputVO;
import org.devocative.demeter.vo.UserVO;

import java.util.List;

public interface IUserService {
	List<User> list();

	User loadByUsername(String username);

	void saveOrUpdate(User user, String password);

	UserVO createOrUpdateUser(UserInputVO userInputVO);

	UserVO loadVOByUsername(String username);

	UserVO getUserVO(User user);
}
