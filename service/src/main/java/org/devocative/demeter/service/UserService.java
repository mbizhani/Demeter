package org.devocative.demeter.service;

import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.EUserStatus;
import org.devocative.demeter.entity.Person;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IPageService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("dmtUserService")
public class UserService implements IUserService {
	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IPageService pageService;

	@Override
	public List<User> list() {
		return persistorService.list(User.class);
	}

	@Override
	public User loadByUsername(String username) {
		return persistorService
			.createQueryBuilder()
			.addFrom(User.class, "ent")
			.addWhere("and ent.username = :uname")
			.addParam("uname", username)
			.object();
	}

	@Override
	public void saveOrUpdate(User user) {
		user.getPerson().setHasUser(true);

		//TODO save password encrypted

		persistorService.saveOrUpdate(user.getPerson());
		persistorService.saveOrUpdate(user);
		persistorService.endSession();
	}

	@Override
	public UserVO createOrUpdateUser(String username, String password, String firstName, String lastName) {
		User user = loadByUsername(username);

		if (user == null) {
			user = new User();
			user.setUsername(username);
			user.setPassword(password);
			user.setLastLoginDate(new Date());
			user.setStatus(EUserStatus.ENABLED);

			Person person = user.getPerson();
			if (person == null) {
				person = new Person();
				user.setPerson(person);
			}
			person.setFirstName(firstName);
			person.setLastName(lastName);

			saveOrUpdate(user);
		} else {
			user.setStatus(EUserStatus.ENABLED);
			persistorService.saveOrUpdate(user);
			persistorService.endSession();
		}

		return getUserVO(user);
	}

	@Override
	public UserVO loadVOByUsername(String username) {
		User user = loadByUsername(username);
		if (user == null) {
			throw new DemeterException(DemeterErrorCode.InvalidUser, username);
		}
		return getUserVO(user);
	}

	// ------------------------------

	private UserVO getUserVO(User user) {
		return new UserVO(user.getId(), user.getUsername(), user.getPerson().getFirstName(), user.getPerson().getLastName());
	}
}
