package org.devocative.demeter.vo;

import org.devocative.demeter.entity.EAuthMechanism;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.EUserStatus;

import java.io.Serializable;

public class UserInputVO implements Serializable {
	private static final long serialVersionUID = -8601295449952419297L;

	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private boolean admin = false;
	private EUserStatus status = EUserStatus.ENABLED;
	private EAuthMechanism authMechanism;
	private ERowMod rowMod;

	private String otherId;

	private Integer sessionTimeout = 60;

	// ------------------------------

	public UserInputVO() {
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

	public boolean isAdmin() {
		return admin;
	}

	public UserInputVO setAdmin(boolean admin) {
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

	public ERowMod getRowMod() {
		return rowMod;
	}

	public UserInputVO setRowMod(ERowMod rowMod) {
		this.rowMod = rowMod;
		return this;
	}

	public String getOtherId() {
		return otherId;
	}

	public UserInputVO setOtherId(String otherId) {
		this.otherId = otherId;
		return this;
	}

	public Integer getSessionTimeout() {
		return sessionTimeout;
	}

	public UserInputVO setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
		return this;
	}
}
