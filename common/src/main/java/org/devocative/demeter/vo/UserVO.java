package org.devocative.demeter.vo;

import org.devocative.demeter.entity.*;

import java.io.Serializable;
import java.util.*;

public class UserVO implements Serializable {
	private static final long serialVersionUID = -8402970677148363395L;

	private Long userId;
	private String username;
	private String firstName;
	private String lastName;

	private boolean authenticated = false;
	private boolean root = false;
	private boolean admin = false;
	private EAuthMechanism authMechanism;
	private Set<Role> roles = new HashSet<>();
	private Set<String> permissions = new HashSet<>();
	private Set<String> denials = new HashSet<>();

	private PageVO pageVO;
	private Map<String, Object> otherProfileInfo = new HashMap<>();

	private String otherId;

	private ELocale locale;
	private ECalendar calendar;
	private ELayoutDirection layoutDirection;
	private EDatePatternType datePatternType;
	private EDateTimePatternType dateTimePatternType;
	private Integer sessionTimeout;

	// ------------------------------

	public UserVO(Long userId, String username, String firstName, String lastName) {
		this.userId = userId;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.root = "root".equals(username);
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
		return root;
	}

	public boolean hasAccessToURI(String uri) {
		return pageVO.getAccessibleUri().contains(uri);
	}

	public Map<String, Set<DPageInstance>> getMainMenuEntries() {
		return pageVO.getMainMenuEntries();
	}

	public boolean hasPermission(IPrivilegeKey privilegeKey) {
		return
			isRoot() ||
				!denials.contains(privilegeKey.getName()) &&
					(isAdmin() || permissions.contains(privilegeKey.getName()));
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

	public EAuthMechanism getAuthMechanism() {
		return authMechanism;
	}

	public UserVO setAuthMechanism(EAuthMechanism authMechanism) {
		this.authMechanism = authMechanism;
		return this;
	}

	public Collection<Role> getRoles() {
		return roles;
	}

	public UserVO addRole(Role role) {
		roles.add(role);

		if (role.getDenials() != null) {
			for (Privilege privilege : role.getDenials()) {
				addDenial(privilege.getName());
			}
		}

		// Admin user has permission to every thing by default
		if (!isAdmin() && role.getPermissions() != null) {
			for (Privilege privilege : role.getPermissions()) {
				addPermission(privilege.getName());
			}
		}

		return this;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public Set<String> getDenials() {
		return denials;
	}

	public UserVO addPermission(String privilegeKey) {
		permissions.add(privilegeKey);
		return this;
	}

	public UserVO addDenial(String privilegeKey) {
		denials.add(privilegeKey);
		return this;
	}

	public boolean isPageEmpty() {
		return pageVO == null;
	}

	public UserVO setPageVO(PageVO pageVO) {
		this.pageVO = pageVO;
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

	public ELocale getLocale() {
		return locale;
	}

	public UserVO setLocale(ELocale locale) {
		this.locale = locale;
		return this;
	}

	public ECalendar getCalendar() {
		return calendar;
	}

	public UserVO setCalendar(ECalendar calendar) {
		this.calendar = calendar;
		return this;
	}

	public ELayoutDirection getLayoutDirection() {
		return layoutDirection;
	}

	public UserVO setLayoutDirection(ELayoutDirection layoutDirection) {
		this.layoutDirection = layoutDirection;
		return this;
	}

	public EDatePatternType getDatePatternType() {
		return datePatternType;
	}

	public UserVO setDatePatternType(EDatePatternType datePatternType) {
		this.datePatternType = datePatternType;
		return this;
	}

	public EDateTimePatternType getDateTimePatternType() {
		return dateTimePatternType;
	}

	public UserVO setDateTimePatternType(EDateTimePatternType dateTimePatternType) {
		this.dateTimePatternType = dateTimePatternType;
		return this;
	}

	public Integer getSessionTimeout() {
		return sessionTimeout;
	}

	public UserVO setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
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

	// ------------------------------

	public static class PageVO implements Serializable {
		private static final long serialVersionUID = 5722909672657821898L;

		private Set<String> accessibleUri;
		private Map<String, Set<DPageInstance>> mainMenuEntries;

		// ------------------------------

		public PageVO(Set<String> accessibleUri, Map<String, Set<DPageInstance>> mainMenuEntries) {
			this.accessibleUri = accessibleUri;
			this.mainMenuEntries = mainMenuEntries;
		}

		// ------------------------------

		public Set<String> getAccessibleUri() {
			return accessibleUri;
		}

		public Map<String, Set<DPageInstance>> getMainMenuEntries() {
			return mainMenuEntries;
		}

		@Override
		public String toString() {
			return String.format("URIs=%s, Menu=%s", accessibleUri, mainMenuEntries);
		}
	}
}
