package org.devocative.demeter.service;

import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dmtUserService")
public class UserService implements IUserService {
	@Autowired
	private IPersistorService persistorService;

	@Override
	public User getUser(String username) {
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
		persistorService.commitOrRollback();
	}
}
