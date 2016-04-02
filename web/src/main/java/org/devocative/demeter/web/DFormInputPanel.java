package org.devocative.demeter.web;

import org.apache.wicket.model.IModel;
import org.devocative.wickomp.WFormInputPanel;

public class DFormInputPanel<T> extends WFormInputPanel<T> {
	public DFormInputPanel(String id, IModel<T> model) {
		super(id, model);
	}
}
