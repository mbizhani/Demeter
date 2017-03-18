//overwrite
package org.devocative.demeter.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class UserFVO implements Serializable {
	private static final long serialVersionUID = 1276322405L;

	private String username;
	private List<EAuthMechanism> authMechanism;
	private List<EUserStatus> status;
	private List<ELocale> locale;
	private List<ECalendar> calendarType;
	private List<ELayoutDirection> layoutDirectionType;
	private List<EDatePatternType> datePatternType;
	private List<EDateTimePatternType> dateTimePatternType;
	private RangeVO<Date> lastLoginDate;
	private Boolean admin;
	private RangeVO<Integer> sessionTimeout;
	private List<Role> roles;

	// ------------------------------

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<EAuthMechanism> getAuthMechanism() {
		return authMechanism;
	}

	public void setAuthMechanism(List<EAuthMechanism> authMechanism) {
		this.authMechanism = authMechanism;
	}

	public List<EUserStatus> getStatus() {
		return status;
	}

	public void setStatus(List<EUserStatus> status) {
		this.status = status;
	}

	public List<ELocale> getLocale() {
		return locale;
	}

	public void setLocale(List<ELocale> locale) {
		this.locale = locale;
	}

	public List<ECalendar> getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(List<ECalendar> calendarType) {
		this.calendarType = calendarType;
	}

	public List<ELayoutDirection> getLayoutDirectionType() {
		return layoutDirectionType;
	}

	public void setLayoutDirectionType(List<ELayoutDirection> layoutDirectionType) {
		this.layoutDirectionType = layoutDirectionType;
	}

	public List<EDatePatternType> getDatePatternType() {
		return datePatternType;
	}

	public void setDatePatternType(List<EDatePatternType> datePatternType) {
		this.datePatternType = datePatternType;
	}

	public List<EDateTimePatternType> getDateTimePatternType() {
		return dateTimePatternType;
	}

	public void setDateTimePatternType(List<EDateTimePatternType> dateTimePatternType) {
		this.dateTimePatternType = dateTimePatternType;
	}

	public RangeVO<Date> getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(RangeVO<Date> lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public RangeVO<Integer> getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(RangeVO<Integer> sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}