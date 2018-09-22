package org.devocative.demeter.web.panel.dynamic;

import org.apache.wicket.model.IModel;
import org.devocative.wickomp.html.icon.IconFont;

import java.io.Serializable;

public class SubmitButtonInfo implements Serializable {
	private static final long serialVersionUID = 1158112083395749777L;

	private String name;
	private IModel<String> caption;
	private IconFont icon;

	// ------------------------------

	public SubmitButtonInfo(String name, IModel<String> caption) {
		this(name, caption, null);
	}

	public SubmitButtonInfo(String name, IModel<String> caption, IconFont icon) {
		this.name = name;
		this.caption = caption;
		this.icon = icon;
	}

	// ------------------------------

	public String getName() {
		return name;
	}

	public IModel<String> getCaption() {
		return caption;
	}

	public IconFont getIcon() {
		return icon;
	}
}
