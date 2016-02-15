package org.devocative.demeter.iservice;

import org.devocative.demeter.vo.UserVO;

public interface ISecurityService {
	UserVO getCurrentUser();

	void authenticate(UserVO userVO);

	void authenticate(String username, String password);

	void signOut();
}
