package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.*;
import org.devocative.demeter.vo.filter.RoleFVO;
import org.devocative.demeter.vo.input.RoleIVO;

import java.util.List;

public interface IRoleService {
	void saveOrUpdate(Role entity);

	void saveOrUpdate(RoleIVO vo);

	Role load(Long id);

	Role loadByName(String name);

	List<Role> list();

	List<Role> search(RoleFVO filter, long pageIndex, long pageSize);

	long count(RoleFVO filter);

	List<Privilege> getPermissionsList();

	List<Privilege> getDenialsList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	Role createOrUpdate(String name, ERowMode rowMode, ERoleMode roleMode);

	Role createOnly(String name, ERowMode rowMode, ERoleMode roleMode);
}