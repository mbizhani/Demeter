package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DBConstraintViolationException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.IPersonService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.UserInputVO;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.vo.filter.UserFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service("dmtUserService")
public class UserService implements IUserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IPersonService personService;

	// ------------------------------

	@Transactional
	@Override
	public void saveOrUpdate(User entity) {
		persistorService.assertActiveTrx();

		personService.saveOrUpdate(entity.getPerson());
		entity.setId(entity.getPerson().getId());
		try {
			persistorService.saveOrUpdate(entity);
		} catch (DBConstraintViolationException e) {
			if (e.isConstraint(User.UQ_CONST)) {
				throw new DemeterException(DemeterErrorCode.DuplicateUsername);
			}
		}
	}

	@Override
	public User load(Long id) {
		return persistorService.createQueryBuilder()
			.addFrom(User.class, "ent")
			.addJoin("prs", "ent.person", EJoinMode.LeftFetch)
			.addWhere("and ent.id = :id", "id", id)
			.object();
	}

	@Override
	public User loadByUsername(String username) {
		return persistorService
			.createQueryBuilder()
			.addFrom(User.class, "ent")
			.addJoin("prs", "ent.person", EJoinMode.LeftFetch)
			.addWhere("and ent.username = :username", "username", username)
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
			.addWhere("and ent.roleMode <> :mode")
			.addParam("mode", ERoleMode.DYNAMIC)
			.setOrderBy("ent.name")
			.list();
	}

	@Override
	public List<Privilege> getPermissionsList() {
		return persistorService.list(Privilege.class);
	}

	@Override
	public List<Privilege> getDenialsList() {
		return persistorService.list(Privilege.class);
	}

	// ==============================

	@Transactional
	@Override
	public void saveOrUpdate(User user, String password) {
		user.getPerson().setHasUser(true);

		if (password != null) {
			user.setPassword(StringEncryptorUtil.hash(password));
		}

		saveOrUpdate(user);
	}

	@Transactional
	@Override
	public void updateUser(User user, String password, String oldPassword) {
		String old = StringEncryptorUtil.hash(oldPassword);

		if (!old.equals(user.getPassword())) {
			throw new DemeterException(DemeterErrorCode.InvalidCurrentPassword);
		}

		user.setPassword(StringEncryptorUtil.hash(password));
		saveOrUpdate(user);
	}

	@Transactional
	@Override
	public UserVO createOrUpdateUser(UserInputVO userInputVO, User user, boolean forceUpdate) {
		if (user == null) {
			user = loadByUsername(userInputVO.getUsername());
		}

		String password = userInputVO.getPassword();
		if (user == null) {
			user = new User();
		} else {
			password = null;
		}

		if (user.getId() == null || forceUpdate) {
			user.setUsername(userInputVO.getUsername());

			if (userInputVO.getStatus() != null) {
				user.setStatus(userInputVO.getStatus());
			}

			if (userInputVO.getAdmin() != null) {
				user.setAdmin(userInputVO.getAdmin());
			}

			user.setAuthMechanism(userInputVO.getAuthMechanism());
			if (userInputVO.getSessionTimeout() != null) {
				user.setSessionTimeout(userInputVO.getSessionTimeout());
			}

			Person person = user.getPerson();
			if (person == null) {
				person = new Person();
				user.setPerson(person);
			}
			person.setFirstName(userInputVO.getFirstName());
			person.setLastName(userInputVO.getLastName());

			if (userInputVO.getRowMode() != null) {
				person.setRowMode(userInputVO.getRowMode());
			}

			saveOrUpdate(user, password);
		}

		return getUserVO(user)
			.setOtherId(userInputVO.getOtherId())
			.updateOtherData(userInputVO.getOtherData());
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
		final ELocale defLocale = ELocale.findByCode(ConfigUtil.getString(DemeterConfigKey.UserDefaultLocale));
		final ECalendar defCalendar = ECalendar.findByName(ConfigUtil.getString(DemeterConfigKey.UserDefaultCalendar));
		final String defTimeZone = ConfigUtil.getString(DemeterConfigKey.UserDefaultTimeZone);

		Integer sto = user.getSessionTimeout();
		if (sto == null) {
			if (EAuthMechanism.DATABASE.equals(user.getAuthMechanism())) {
				sto = ConfigUtil.getInteger(DemeterConfigKey.STO_Database);
			} else if (EAuthMechanism.LDAP.equals(user.getAuthMechanism())) {
				sto = ConfigUtil.getInteger(DemeterConfigKey.STO_LDAP);
			} else if (EAuthMechanism.OTHER.equals(user.getAuthMechanism())) {
				sto = ConfigUtil.getInteger(DemeterConfigKey.STO_Other);
			}

			if (user.getAdmin()) {
				sto = ConfigUtil.getInteger(DemeterConfigKey.STO_Admin);
			}
		}

		ELocale userLocale = user.getLocale() != null ? user.getLocale() : defLocale;
		Person person = user.getPersonSafely();
		UserVO userVO = new UserVO(user.getId(), user.getUsername(), person.getFirstName(), person.getLastName())
			.setAdmin(user.getAdmin())
			.setAuthMechanism(user.getAuthMechanism())

			.setLocale(userLocale)
			.setLayoutDirection(userLocale.getLayoutDirection())
			.setCalendar(
				user.getCalendarType() != null ?
					user.getCalendarType() :
					defCalendar != null ? defCalendar :
						userLocale.getDefaultCalendar())
			.setTimeZone(TimeZone.getTimeZone(
				user.getTimeZone() != null ? user.getTimeZone() : defTimeZone))
			.setDatePatternType(user.getDatePatternType() != null ? user.getDatePatternType() : EDatePatternType.P01)
			.setDateTimePatternType(user.getDateTimePatternType() != null ? user.getDateTimePatternType() : EDateTimePatternType.P01)
			.setSessionTimeout(sto);

		if (user.getRoles() != null) {
			user.getRoles().forEach(userVO::addRole);
		}

		if (user.getPermissions() != null) {
			for (Privilege privilege : user.getPermissions()) {
				userVO.addPermission(privilege.getName());
			}
		}

		if (user.getDenials() != null) {
			for (Privilege privilege : user.getDenials()) {
				userVO.addDenial(privilege.getName());
			}
		}

		logger.info("UserVO: username=[{}] permissions=[{}], denials=[{}]",
			userVO.getUsername(), userVO.getPermissions(), userVO.getDenials());

		return userVO;
	}

	@Transactional
	@Override
	public void updateLastLoginDate(String username) {
		persistorService.createQueryBuilder()
			.addSelect("update User ent set ent.lastLoginDate = :lld where ent.username = :username")
			.addParam("lld", new Date())
			.addParam("username", username)
			.update();
	}
}