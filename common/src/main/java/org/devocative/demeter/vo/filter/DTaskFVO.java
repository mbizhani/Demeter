package org.devocative.demeter.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.entity.DTaskState;
import org.devocative.demeter.iservice.persistor.FilterOption;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DTaskFVO implements Serializable {
	private static final long serialVersionUID = -2461425552334924991L;

	@FilterOption(useLike = true)
	private String id;

	@FilterOption(useLike = true)
	private String type;

	private RangeVO<Date> startDate;

	private List<DTaskState> state;

	@FilterOption(useLike = true)
	private String currentUser;

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

	public RangeVO<Date> getStartDate() {
		return startDate;
	}

	public void setStartDate(RangeVO<Date> startDate) {
		this.startDate = startDate;
	}

	public List<DTaskState> getState() {
		return state;
	}

	public void setState(List<DTaskState> state) {
		this.state = state;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}
}
