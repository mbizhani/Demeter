package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("key")
public class XKey {
	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private Boolean required;

	@XStreamAsAttribute
	private EKeyType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public EKeyType getType() {
		return type;
	}

	public void setType(EKeyType type) {
		this.type = type;
	}
}
