package org.devocative.demeter.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.form.WAjaxButton;
import org.devocative.wickomp.html.HTMLBase;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.wrcs.EasyUIBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class DAjaxButton extends WAjaxButton {
	private static final Logger logger = LoggerFactory.getLogger(DAjaxButton.class);

	public DAjaxButton(String id) {
		this(id, null, null);
	}

	public DAjaxButton(String id, IModel<String> caption) {
		this(id, caption, null);
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

	@Override
	protected void onException(AjaxRequestTarget target, Exception e) {
		logger.error("DAjaxButton: " + getId(), e);

		if (e instanceof DModuleException) {
			DModuleException de = (DModuleException) e;
			String error = getString(de.getMessage(), null, de.getDefaultDescription());
			if (de.getErrorParameter() != null) {
				error += ": " + de.getErrorParameter();
			}
			WMessager.show(getString("label.error", null, "Error"), error, target);
		} else {
			super.onException(target, e);
		}
	}
}
