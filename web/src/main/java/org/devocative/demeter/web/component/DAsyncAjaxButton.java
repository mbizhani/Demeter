package org.devocative.demeter.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.devocative.wickomp.form.WAsyncAjaxButton;
import org.devocative.wickomp.html.HTMLBase;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.wrcs.EasyUIBehavior;

import java.io.Serializable;
import java.util.List;

public abstract class DAsyncAjaxButton extends WAsyncAjaxButton {
	private static final long serialVersionUID = -8540976636639102816L;

	public DAsyncAjaxButton(String id) {
		this(id, null, null);
	}

	public DAsyncAjaxButton(String id, IModel<String> caption) {
		this(id, caption, null);
	}

	// Main Constructor
	public DAsyncAjaxButton(String id, IModel<String> caption, HTMLBase icon) {
		super(id, caption, icon);

		add(new EasyUIBehavior());
	}

	@Override
	protected void onError(AjaxRequestTarget target, List<Serializable> errors) {
		WMessager.show(getString("label.error", null, "Error"), errors, target);
	}
}
