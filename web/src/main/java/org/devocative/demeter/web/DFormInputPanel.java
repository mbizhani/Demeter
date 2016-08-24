package org.devocative.demeter.web;

import org.apache.wicket.model.IModel;
import org.devocative.wickomp.WFormInputPanel;

public class DFormInputPanel<T> extends WFormInputPanel<T> {
	private static final long serialVersionUID = -2439584616340309300L;

	public DFormInputPanel(String id, IModel<T> model) {
		super(id, model);
	}
}
