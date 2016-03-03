package org.devocative.demeter.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.devocative.demeter.web.DemeterExceptionToMessageHandler;
import org.devocative.wickomp.form.WAjaxButton;
import org.devocative.wickomp.html.HTMLBase;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.wrcs.EasyUIBehavior;

import java.io.Serializable;
import java.util.List;

public class DAjaxButton extends WAjaxButton {
	public DAjaxButton(String id) {
		this(id, null, null);
	}

	public DAjaxButton(String id, IModel<String> caption) {
		this(id, caption, null);
	}

	// Main Constructor
	public DAjaxButton(String id, IModel<String> caption, HTMLBase icon) {
		super(id, caption, icon);

		setExceptionToMessageHandler(DemeterExceptionToMessageHandler.get());

		add(new EasyUIBehavior());
	}

	@Override
	protected void onError(AjaxRequestTarget target, List<Serializable> errors) {
		WMessager.show(getString("label.error", null, "Error"), errors, target);
	}
}
