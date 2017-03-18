package org.devocative.demeter.iservice;

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

	// ==============================

	void saveOrUpdate(User user, String password);

	UserVO createOrUpdateUser(UserInputVO userInputVO);

	UserVO loadVOByUsername(String username);

	UserVO getUserVO(User user);
}
