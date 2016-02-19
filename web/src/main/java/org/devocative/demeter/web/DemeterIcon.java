package org.devocative.demeter.web;

import org.apache.wicket.model.ResourceModel;
import org.devocative.wickomp.html.HTMLBase;
import org.devocative.wickomp.html.icon.FontAwesome;

public class DemeterIcon {
	public static final HTMLBase EDIT = new FontAwesome("pencil", new ResourceModel("label.edit", "Edit"));
	public static final HTMLBase EXECUTE = new FontAwesome("cogs", new ResourceModel("label.execute", "Execute")).setStyleClass("ic-imp");
	public static final HTMLBase SEARCH = new FontAwesome("search", new ResourceModel("label.search", "Search"));
	public static final HTMLBase SAVE = new FontAwesome("floppy-o ", new ResourceModel("label.save", "Save"));

	public static final HTMLBase EXPORT_EXCEL = new FontAwesome("file-excel-o", new ResourceModel("label.export.excel", "Export Excel")).setStyleClass("ic-imp");
	public static final HTMLBase EXPAND = new FontAwesome("expand", new ResourceModel("label.nodes.expand", "Expand"));
	public static final HTMLBase COLLAPSE = new FontAwesome("compress", new ResourceModel("label.nodes.collapse", "Collapse"));

	public static final HTMLBase TRUE = new FontAwesome("check", new ResourceModel("label.true", "True")).setStyleClass("ic-true");
	public static final HTMLBase FALSE = new FontAwesome("times", new ResourceModel("label.false", "False")).setStyleClass("ic-false");
}
