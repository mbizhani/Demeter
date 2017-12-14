package org.devocative.demeter.web;

import org.apache.wicket.model.ResourceModel;
import org.devocative.wickomp.html.icon.FontAwesome;
import org.devocative.wickomp.html.icon.IconFont;

public class DemeterIcon {
	public static final IconFont EDIT = new FontAwesome("pencil", new ResourceModel("label.edit", "Edit"));
	public static final IconFont EXECUTE = new FontAwesome("cogs", new ResourceModel("label.execute", "Execute")).setStyleClass("ic-imp");
	public static final IconFont SAVE = new FontAwesome("floppy-o", new ResourceModel("label.save", "Save"));
	public static final IconFont REMOVE = new FontAwesome("times", new ResourceModel("label.remove", "Remove")).setStyleClass("ic-false");

	public static final IconFont ADD = new FontAwesome("plus", new ResourceModel("label.add", "Add"));
	public static final IconFont ADD_USER = new FontAwesome("user-plus", new ResourceModel("label.add.user", "Add User"));

	public static final IconFont EXPORT_EXCEL = new FontAwesome("file-excel-o", new ResourceModel("label.export.excel", "Export Excel")).setStyleClass("ic-imp");
	public static final IconFont COLLAPSE = new FontAwesome("compress", new ResourceModel("label.nodes.collapse", "Collapse"));

	public static final IconFont TRUE = new FontAwesome("check", new ResourceModel("label.true", "True")).setStyleClass("ic-true");
	public static final IconFont FALSE = new FontAwesome("times", new ResourceModel("label.false", "False")).setStyleClass("ic-false");

	public static final IconFont ATTACHMENT = new FontAwesome("paperclip", new ResourceModel("label.attachment", "Attachment"));
	public static final IconFont INFO = new FontAwesome("bug", new ResourceModel("label.info", "Info"));

	public static final IconFont SHOW = new FontAwesome("eye", new ResourceModel("label.show", "Show"));

	public static final IconFont EXPORT_IMPORT = new FontAwesome("exchange", new ResourceModel("label.exchange", "Export/Import"));

	// ---------- THE SAME NAME

	public static final IconFont DOWNLOAD = new FontAwesome("download", new ResourceModel("label.download", "Download"));
	public static final IconFont EXPAND = new FontAwesome("expand", new ResourceModel("label.nodes.expand", "Expand"));
	public static final IconFont SEARCH = new FontAwesome("search", new ResourceModel("label.search", "Search"));
	public static final IconFont SHIELD = new FontAwesome("shield", new ResourceModel("label.shield", "Shield"));
	public static final IconFont STOP_CIRCLE = new FontAwesome("stop-circle", new ResourceModel("label.stop", "Stop"));
	public static final IconFont UPLOAD = new FontAwesome("upload", new ResourceModel("label.upload", "Upload"));
}
