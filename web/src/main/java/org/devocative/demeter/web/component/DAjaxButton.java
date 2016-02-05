package org.devocative.demeter.web.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.form.WAjaxButton;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.wrcs.EasyUIBehavior;

import java.io.Serializable;
import java.util.List;

public class DAjaxButton extends WAjaxButton {
	public DAjaxButton(String id) {
		super(id);

		add(new EasyUIBehavior());
	}

	@Override
	protected void onError(AjaxRequestTarget target, List<Serializable> errors) {
		WMessager.show(getString("label.error", null, "Error"), errors, target);
	}

	@Override
	protected void onException(AjaxRequestTarget target, Exception e) {
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
