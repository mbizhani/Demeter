package org.devocative.demeter.vo;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.entity.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserVO implements Serializable {
	private static final long serialVersionUID = -8402970677148363395L;

	private Long userId;
	private String username;
	private String firstName;
	private String lastName;

	private boolean authenticated = false;
	private boolean admin = false;
	private Map<String, List<DPageInstance>> defaultPages;
	private Integer sessionTimeout = ConfigUtil.getInteger(DemeterConfigKey.DefaultSessionTimeoutInterval);
	private Map<String, Object> otherProfileInfo = new HashMap<>();

	private String otherId;

	// ------------------------------

	public UserVO(Long userId, String username, String firstName, String lastName) {
		this.userId = userId;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	// ------------------------------

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFullName() {
		return (getFirstName() != null ? getFirstName() : "") + " " +
			(getLastName() != null ? getLastName() : "");
	}

	public boolean isRoot() {
		return "root".equals(username);
	}

	// ---------------

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isAdmin() {
		return admin;
	}

	public UserVO setAdmin(boolean admin) {
		this.admin = admin;
		return this;
	}

	public Map<String, List<DPageInstance>> getDefaultPages() {
		return defaultPages;
	}

	public void setDefaultPages(Map<String, List<DPageInstance>> defaultPages) {
		this.defaultPages = defaultPages;
	}

	public Integer getSessionTimeout() {
		return isAuthenticated() ? sessionTimeout : -1;
	}

	public UserVO setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
		return this;
	}

	public Object getOtherProfileInfo(String key) {
		return otherProfileInfo.get(key);
	}

	public String getOtherId() {
		return otherId;
	}

	public UserVO setOtherId(String otherId) {
		this.otherId = otherId;
		return this;
	}

	// ------------------------------

	public UserVO addOtherProfileInfo(String key, Object info) {
		otherProfileInfo.put(key, info);
		return this;
	}

	public Object removeOtherProfileInfo(String key) {
		return otherProfileInfo.remove(key);
	}

	public User toUser() {
		User user = new User();
		user.setId(getUserId());
		return user;
	}

	// ------------------------------

	@Override
	public String toString() {
		return username;
	}
}
