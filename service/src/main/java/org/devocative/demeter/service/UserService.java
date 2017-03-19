package org.devocative.demeter.service;

import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.Person;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
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
		return persistorService.list(Role.class);
	}

	// ==============================

	@Override
	public void saveOrUpdate(User user, String password) {
		user.getPerson().setHasUser(true);

		if (user.getUsername() != null) {
			user.setUsername(user.getUsername().toLowerCase());
		}

		if (password != null) {
			user.setPassword(StringEncryptorUtil.hash(password));
		}

		persistorService.saveOrUpdate(user.getPerson());
		persistorService.saveOrUpdate(user);
		persistorService.endSession();
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
		return new UserVO(user.getId(), user.getUsername(), user.getPerson().getFirstName(), user.getPerson().getLastName())
			.setAdmin(user.getAdmin());
	}
}