package org.devocative.demeter.service;

import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.Person;
import org.devocative.demeter.entity.Privilege;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IPersonService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.UserInputVO;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.vo.filter.UserFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dmtUserService")
public class UserService implements IUserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IPersonService personService;

	// ------------------------------

	@Override
	public void saveOrUpdate(User entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public User load(Long id) {
		return persistorService.get(User.class, id);
	}

	@Override
	public User loadByUsername(String username) {
		return persistorService
			.createQueryBuilder()
			.addFrom(User.class, "ent")
			.addWhere("and ent.username = :username")
			.addParam("username", username)
			.object();
	}

	@Override
	public List<User> list() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> search(UserFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(User.class, "ent")
			.applyFilter(User.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(UserFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(User.class, "ent")
			.applyFilter(User.class, "ent", filter)
			.object();
	}

	@Override
	public List<Role> getRolesList() {
		return persistorService.createQueryBuilder()
			.addFrom(Role.class, "ent")
			.addWhere("and ent.dynamic = false")
			.list();
	}

	@Override
	public List<Privilege> getAuthorizationsList() {
		return persistorService.list(Privilege.class);
	}

	// ==============================

	@Override
	public void saveOrUpdate(User user, String password) {
		user.getPerson().setHasUser(true);

		if (password != null) {
			user.setPassword(StringEncryptorUtil.hash(password));
		}

		personService.saveOrUpdate(user.getPerson());
		saveOrUpdate(user);
		persistorService.commitOrRollback();
	}

	@Override
	public void updateUser(User user, String password, String oldPassword) {
		String old = StringEncryptorUtil.hash(oldPassword);

		if (!old.equals(user.getPassword())) {
			throw new DemeterException(DemeterErrorCode.InvalidCurrentPassword);
		}

		user.setPassword(StringEncryptorUtil.hash(password));
		saveOrUpdate(user);
	}

	@Override
	public UserVO createOrUpdateUser(UserInputVO userInputVO) {
		User user = loadByUsername(userInputVO.getUsername());

		String password = userInputVO.getPassword();
		if (user == null) {
			user = new User();
		} else {
			password = null;
		}

		user.setUsername(userInputVO.getUsername());
		user.setStatus(userInputVO.getStatus());
		user.setAdmin(userInputVO.isAdmin());
		user.setAuthMechanism(userInputVO.getAuthMechanism());
		user.setSessionTimeout(userInputVO.getSessionTimeout());

		Person person = user.getPerson();
		if (person == null) {
			person = new Person();
			user.setPerson(person);
		}
		person.setFirstName(userInputVO.getFirstName());
		person.setLastName(userInputVO.getLastName());
		person.setRowMod(userInputVO.getRowMod());

		saveOrUpdate(user, password);
		return getUserVO(user)
			.setOtherId(userInputVO.getOtherId());
	}

	@Override
	public UserVO loadVOByUsername(String username) {
		User user = loadByUsername(username);
		if (user == null) {
			throw new DemeterException(DemeterErrorCode.InvalidUser, username);
		}
		return getUserVO(user);
	}

	@Override
	public UserVO getUserVO(User user) {
		UserVO userVO = new UserVO(user.getId(), user.getUsername(), user.getPerson().getFirstName(), user.getPerson().getLastName())
			.setAdmin(user.getAdmin())
			.setAuthMechanism(user.getAuthMechanism());

		if (user.getRoles() != null) {
			for (Role role : user.getRoles()) {
				userVO.addRole(role);

				if (role.getDenials() != null) {
					for (Privilege privilege : role.getDenials()) {
						userVO.addDenial(privilege.getName());
					}
				}

				// Admin user has permission to every thing by default
				if (!user.getAdmin() && role.getPermissions() != null) {
					for (Privilege privilege : role.getPermissions()) {
						userVO.addPermission(privilege.getName());
					}
				}
			}
		}

		if (user.getAuthorizations() != null) {
			for (Privilege privilege : user.getAuthorizations()) {
				if (user.getAdmin()) {
					userVO.addDenial(privilege.getName());
				} else {
					userVO.addPermission(privilege.getName());
				}
			}
		}

		logger.info("UserVO: permissions={}, denials={}", userVO.getPermissions(), userVO.getDenials());

		return userVO;
	}
}