package org.devocative.demeter.web.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.IModel;
import org.devocative.demeter.imodule.DModuleException;
import org.devocative.wickomp.html.HTMLBase;
import org.devocative.wickomp.html.WMessager;
import org.devocative.wickomp.wrcs.EasyUIBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class DButton extends Button {
	private static final Logger logger = LoggerFactory.getLogger(DButton.class);

	private IModel<String> caption;
	private HTMLBase icon;

	public DButton(String id) {
		this(id, null, null);
	}

	public DButton(String id, IModel<String> caption) {
		this(id, caption, null);
	}

	// Main constructor
	public DButton(String id, IModel<String> caption, HTMLBase icon) {
		super(id);
		this.caption = caption;
		this.icon = icon;

		add(new EasyUIBehavior());
	}

	@Override
	public final void onError() {
	}

	@Override
	public final void onSubmit() {
		try {
			onFormSubmit();
		} catch (Exception e) {
			logger.error("DButton.onFormSubmit(): ", e);

			if (e instanceof DModuleException) {
				DModuleException de = (DModuleException) e;
				String error = getString(de.getMessage(), null, de.getDefaultDescription());
				if (de.getErrorParameter() != null) {
					error += ": " + de.getErrorParameter();
				}
				error(error);
			} else {
				if (e.getMessage() != null) {
					error(e.getMessage());
				} else {
					error(e.toString());
				}
			}
		}
	}


	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		if ("button".equalsIgnoreCase(openTag.getName()) && (caption != null || icon != null)) {
			String cap = "";
			if (caption != null) {
				cap = caption.getObject();
			}
			if (icon != null) {
				cap += " " + icon.toString();
			}
			replaceComponentTagBody(markupStream, openTag, cap);
		} else {
			super.onComponentTagBody(markupStream, openTag);
		}
	}

	protected void onFormSubmit() {
	}

	@Override
	protected void onComponentTag(final ComponentTag tag) {
		super.onComponentTag(tag);

		tag.put("type", "submit");
		if (caption != null && "input".equalsIgnoreCase(tag.getName())) {
			tag.put("value", caption.getObject());
		}
	}

	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		List<Serializable> errors = WMessager.collectMessages(this);
		if (errors.size() > 0) {
			String st = WMessager.getScript(
				getString("label.error", null, "Error"),
				WMessager.getHtml(errors),
				WMessager.ShowType.show);

			getWebResponse().write(String.format("<script>$(function(){%s});</script>", st));
		}
	}
}
