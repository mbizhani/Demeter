package org.devocative.demeter.iservice;

import org.devocative.demeter.vo.UserVO;

import java.util.List;
import java.util.Map;

public interface ISecurityService {
	UserVO getCurrentUser();

	void authenticate(UserVO userVO);

	void authenticate(String username, String password);

	UserVO authenticateByUrlParams(Map<String, List<String>> params);

	void signOut();

	String getUserDigest(String username);

	UserVO getSystemUser();

	UserVO getGuestUser();
}
