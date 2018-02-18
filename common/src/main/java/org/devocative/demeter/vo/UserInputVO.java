package org.devocative.demeter.vo;

import org.devocative.demeter.entity.EAuthMechanism;
import org.devocative.demeter.entity.ERowMode;
import org.devocative.demeter.entity.EUserStatus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserInputVO implements Serializable {
	private static final long serialVersionUID = -8601295449952419297L;

	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private Boolean admin;
	private EUserStatus status;
	private EAuthMechanism authMechanism;
	private ERowMode rowMod;
	private Integer sessionTimeout;
	private Set<String> roles = new HashSet<>();

	private String otherId;

	// ------------------------------

	public UserInputVO() {
	}

	public UserInputVO(String username, String firstName, String lastName, EAuthMechanism authMechanism) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.authMechanism = authMechanism;
	}

	public UserInputVO(String username, String password, String firstName, String lastName, EAuthMechanism authMechanism) {
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.authMechanism = authMechanism;
	}

	// ------------------------------

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public EAuthMechanism getAuthMechanism() {
		return authMechanism;
	}

	// ---------------

	public Boolean getAdmin() {
		return admin;
	}

	public UserInputVO setAdmin(Boolean admin) {
		this.admin = admin;
		return this;
	}

	public EUserStatus getStatus() {
		return status;
	}

	public UserInputVO setStatus(EUserStatus status) {
		this.status = status;
		return this;
	}

	public ERowMode getRowMod() {
		return rowMod;
	}

	public UserInputVO setRowMod(ERowMode rowMod) {
		this.rowMod = rowMod;
		return this;
	}

	public Integer getSessionTimeout() {
		return sessionTimeout;
	}

	public UserInputVO setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
		return this;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public UserInputVO addRole(String role) {
		roles.add(role);
		return this;
	}

	public String getOtherId() {
		return otherId;
	}

	public UserInputVO setOtherId(String otherId) {
		this.otherId = otherId;
		return this;
	}
}
