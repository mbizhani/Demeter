package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("entity")
public class XEntity {
	@XStreamAsAttribute
	private String type;

	// ------------------------------

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	// ---------------

	@Override
	public String toString() {
		return getType();
	}
}
