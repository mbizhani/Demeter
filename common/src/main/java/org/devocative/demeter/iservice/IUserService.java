package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.EAuthMechanism;
import org.devocative.demeter.entity.EUserStatus;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.UserVO;

import java.util.List;

public interface IUserService {
	List<User> list();

	User loadByUsername(String username);

	void saveOrUpdate(User user, String password);

	UserVO createOrUpdateUser(String username, String password, String firstName, String lastName,
							  EAuthMechanism authMechanism);

	UserVO createOrUpdateUser(String username, String password, String firstName, String lastName, boolean isAdmin, EUserStatus status, EAuthMechanism authMechanism);

	UserVO loadVOByUsername(String username);

	UserVO getUserVO(User user);
}
