package org.devocative.demeter.web;

import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

public abstract class DPage extends Panel {
	public DPage(String id, List<String> params) {
		super(id);
	}
}
