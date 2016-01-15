package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("dPage")
public class XDPage {
	@XStreamAsAttribute
	private String type;

	@XStreamAsAttribute
	private String title;

	@XStreamAsAttribute
	private String uri;

	@XStreamAsAttribute
	private boolean singleton;

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

	public boolean isSingleton() {
		return singleton;
	}

	public XDPage setSingleton(boolean singleton) {
		this.singleton = singleton;
		return this;
	}
}
