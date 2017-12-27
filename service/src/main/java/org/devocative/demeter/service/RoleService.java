package org.devocative.demeter.service;

import org.devocative.demeter.DBConstraintViolationException;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.Privilege;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.filter.RoleFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dmtRoleService")
public class RoleService implements IRoleService {
	private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(Role entity) {
		if (entity.getRowMod() == null) {
			entity.setRowMod(ERowMod.NORMAL);
		}
		try {
			persistorService.saveOrUpdate(entity);
		} catch (DBConstraintViolationException e) {
			if (e.isConstraint("uk_dmt_role_name")) {
				throw new DemeterException(DemeterErrorCode.DuplicateRoleName);
			}
		}
	}

	@Override
	public Role load(Long id) {
		return persistorService.get(Role.class, id);
	}

	@Override
	public Role loadByName(String name) {
		return persistorService
			.createQueryBuilder()
			.addFrom(Role.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();
	}

	@Override
	public List<Role> list() {
		return persistorService.list(Role.class);
	}

	@Override
	public List<Role> search(RoleFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(Role.class, "ent")
			.applyFilter(Role.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(RoleFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(Role.class, "ent")
			.applyFilter(Role.class, "ent", filter)
			.object();
	}

	@Override
	public List<Privilege> getPermissionsList() {
		return persistorService.list(Privilege.class);
	}

	@Override
	public List<Privilege> getDenialsList() {
		return persistorService.list(Privilege.class);
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

	@Override
	public void createOrUpdateRole(String name, ERowMod rowMod, boolean dynamic) {
		Role role = loadByName(name);
		if (role == null) {
			role = new Role();
			role.setName(name);
		}
		role.setDynamic(dynamic);
		role.setRowMod(rowMod);
		saveOrUpdate(role);
		//persistorService.commitOrRollback();
	}
}