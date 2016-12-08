//overwrite
package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.EFileStatus;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WDateInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class FileStoreFormDPage extends DPage {
	private static final long serialVersionUID = -1120847571L;

	@Inject
	private IFileStoreService fileStoreService;

	private FileStore entity;

	// ------------------------------

	public FileStoreFormDPage(String id) {
		this(id, new FileStore());
	}

	// Main Constructor - For Ajax Call
	public FileStoreFormDPage(String id, FileStore entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public FileStoreFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			fileStoreService.load(Long.valueOf(params.get(0))) :
			new FileStore();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("FileStore.name")));
		floatTable.add(new WSelectionInput("status", EFileStatus.list(), false)
			.setLabel(new ResourceModel("FileStore.status")));
		floatTable.add(new WSelectionInput("mimeType", EMimeType.list(), false)
			.setLabel(new ResourceModel("FileStore.mimeType")));
		floatTable.add(new WTextInput("tag")
			.setLabel(new ResourceModel("FileStore.tag")));
		floatTable.add(new WDateInput("expiration")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("FileStore.expiration")));

		Form<FileStore> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);
		form.add(new DAjaxButton("save", new ResourceModel("label.save")) {
			private static final long serialVersionUID = -1906861675L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				fileStoreService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(FileStoreFormDPage.this, target)) {
					UrlUtil.redirectTo(FileStoreListDPage.class);
				}
			}
		});
		add(form);
	}
}