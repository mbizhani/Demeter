package org.devocative.demeter.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.devocative.wickomp.form.WAjaxButton;
import org.devocative.wickomp.html.HTMLBase;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.wrcs.EasyUIBehavior;

import java.io.Serializable;
import java.util.List;

public abstract class DAjaxButton extends WAjaxButton {
	private static final long serialVersionUID = 9103254439567154781L;

	public DAjaxButton(String id) {
		this(id, null, null);
	}

	public DAjaxButton(String id, IModel<String> caption) {
		this(id, caption, null);
	}

	public DAjaxButton(String id, HTMLBase icon) {
		this(id, null, icon);
	}
	// Main Constructor
	public DAjaxButton(String id, IModel<String> caption, HTMLBase icon) {
		super(id, caption, icon);

		add(new EasyUIBehavior());
	}

	@Override
	protected void onError(AjaxRequestTarget target, List<Serializable> errors) {
		WMessager.show(getString("label.error", null, "Error"), errors, target);
	}
}
