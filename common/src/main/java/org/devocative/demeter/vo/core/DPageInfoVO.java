package org.devocative.demeter.vo.core;

public class DPageInfoVO {
	private String type;
	private String title;
	private String uri;
	private Boolean inMenu;
	private String roles;
	private String icon;

	// ------------------------------

	public DPageInfoVO(String type, String title, String uri, Boolean inMenu, String roles, String icon) {
		this.type = type;
		this.title = title;
		this.uri = uri;
		this.inMenu = inMenu;
		this.roles = roles;
		this.icon = icon;
	}

	// ------------------------------

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getUri() {
		return uri;
	}

	public Boolean getInMenu() {
		return inMenu;
	}

	public String getRoles() {
		return roles;
	}

	public String getIcon() {
		return icon;
	}
}
