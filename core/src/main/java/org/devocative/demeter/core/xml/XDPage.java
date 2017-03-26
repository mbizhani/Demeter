package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("dPage")
public class XDPage {
	@XStreamAsAttribute
	private Boolean inMenu;

	@XStreamAsAttribute
	private String type;

	@XStreamAsAttribute
	private String title;

	@XStreamAsAttribute
	private String uri;

	@XStreamAsAttribute
	private String roles;

	// ------------------------------

	public Boolean getInMenu() {
		return inMenu;
	}

	public XDPage setInMenu(Boolean inMenu) {
		this.inMenu = inMenu;
		return this;
	}

	public String getType() {
		return type;
	}

	public XDPage setType(String type) {
		this.type = type;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public XDPage setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public XDPage setUri(String uri) {
		this.uri = uri;
		return this;
	}

	public String getRoles() {
		return roles;
	}

	public XDPage setRoles(String roles) {
		this.roles = roles;
		return this;
	}
}
