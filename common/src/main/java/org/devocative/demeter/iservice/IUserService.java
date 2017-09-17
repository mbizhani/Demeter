package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.Privilege;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.UserInputVO;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.vo.filter.UserFVO;

import java.util.List;

public interface IUserService {
	void saveOrUpdate(User entity);

	User load(Long id);

	User loadByUsername(String username);

	List<User> list();

	List<User> search(UserFVO filter, long pageIndex, long pageSize);

	long count(UserFVO filter);

	List<Role> getRolesList();

	List<Privilege> getAuthorizationsList();

	// ==============================

	void saveOrUpdate(User user, String password);

	void updateUser(User user, String password, String oldPassword);

	UserVO createOrUpdateUser(UserInputVO userInputVO, User user, boolean forceUpdate);

	UserVO loadVOByUsername(String username);

	UserVO getUserVO(User user);

	void updateLastLoginDate(String username);
}
