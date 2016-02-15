package org.devocative.demeter.vo;

import org.devocative.demeter.entity.DPageInstance;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class UserVO implements Serializable {
	private boolean authenticated = false;
	private Long userId;
	private String username;
	private String firstName;
	private String lastName;
	private Map<String, List<DPageInstance>> defaultPages;

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public Long getUserId() {
		return userId;
	}

	public UserVO setUserId(Long userId) {
		this.userId = userId;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public UserVO setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public UserVO setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public UserVO setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getFullName() {
		return (getFirstName() != null ? getFirstName() : "") + " " +
			(getLastName() != null ? getLastName() : "");
	}

	public Map<String, List<DPageInstance>> getDefaultPages() {
		return defaultPages;
	}

	public void setDefaultPages(Map<String, List<DPageInstance>> defaultPages) {
		this.defaultPages = defaultPages;
	}

	@Override
	public String toString() {
		return username;
	}
}
