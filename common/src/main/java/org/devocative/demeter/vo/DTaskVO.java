package org.devocative.demeter.vo;

import org.devocative.demeter.entity.DTaskState;

import java.io.Serializable;
import java.util.Date;

public class DTaskVO implements Serializable {
	private static final long serialVersionUID = 1059668536444803086L;

	private String id;
	private String type;
	private Date startDate;
	private DTaskState state;
	private String currentUser;

	// ------------------------------

	public DTaskVO() {
	}

	public DTaskVO(String id, String type, Date startDate, DTaskState state, String currentUser) {
		this.id = id;
		this.type = type;
		this.startDate = startDate;
		this.state = state;
		this.currentUser = currentUser;
	}

	// ------------------------------

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public DTaskState getState() {
		return state;
	}

	public void setState(DTaskState state) {
		this.state = state;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	// ---------------

	public String getKey() {
		String key = getType();
		if (getId() != null) {
			key += "_" + getId();
		}
		return key;
	}
}
