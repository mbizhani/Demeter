package org.devocative.demeter.vo.core;

public class DPageInfoVO {
	private String type;
	private String title;
	private String uri;
	private Boolean inMenu;
	private String roles;

	// ------------------------------

	public DPageInfoVO(String type, String title, String uri, Boolean inMenu, String roles) {
		this.type = type;
		this.title = title;
		this.uri = uri;
		this.inMenu = inMenu;
		this.roles = roles;
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
}
