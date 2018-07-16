package org.devocative.demeter.web.panel;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.devocative.adroit.date.UniDate;
import org.devocative.demeter.entity.EFileStorage;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.iservice.FileStoreHandler;
import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.WLabeledFormInputPanel;
import org.devocative.wickomp.form.WFileInput;
import org.devocative.wickomp.form.WSelectionInput;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class FileStoreUploadPanel extends WLabeledFormInputPanel {
	private static final long serialVersionUID = 2952229703389252184L;

	private WSelectionInput fileStoreSelection;
	private WFileInput file;

	private boolean multipleSelection;
	private boolean fileUploadVisible = true;

	@Inject
	private IFileStoreService fileStoreService;

	// ------------------------------

	public FileStoreUploadPanel(String id, boolean multipleSelection) {
		this(id, null, multipleSelection);
	}

	public FileStoreUploadPanel(String id, IModel model, boolean multipleSelection) {
		super(id, model);

		this.multipleSelection = multipleSelection;
	}

	// ------------------------------

	public boolean isFileUploadVisible() {
		return fileUploadVisible;
	}

	public FileStoreUploadPanel setFileUploadVisible(boolean fileUploadVisible) {
		this.fileUploadVisible = fileUploadVisible;
		return this;
	}

	// ------------------------------

	@Override
	public void convertInput() {
		setConvertedInput(fileStoreSelection.getConvertedInput());
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		fileStoreSelection = new WSelectionInput("fileStoreSelection", new Model(), fileStoreService.listByCurrentUserAsCreator(), multipleSelection);
		add(fileStoreSelection);

		file = new WFileInput("uploadFile");
		//file.setRequired(true);

		Form<Void> form = new Form<>("uploadForm");
		form.add(file);
		form.add(new DAjaxButton("upload", DemeterIcon.UPLOAD) {
			private static final long serialVersionUID = 6729731227392480604L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				List<FileUpload> fileUploads = file.getFileUpload();
				if (!fileUploads.isEmpty()) {
					FileUpload fileUpload = fileUploads.get(0);

					FileStoreHandler fileStoreHandler = fileStoreService.create(
						fileUpload.getClientFileName(),
						EFileStorage.DISK,
						EMimeType.BINARY,
						UniDate.now().updateDay(3).toDate());
					try {
						IOUtils.copy(fileUpload.getInputStream(), fileStoreHandler);
						fileStoreHandler.close();

						fileStoreSelection.setModel(new Model<>(fileStoreHandler.getFileStore()));
						fileStoreSelection.updateChoices(target, fileStoreService.listByCurrentUserAsCreator());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new RuntimeException("No file uploaded");
				}
			}
		});
		form.setVisible(fileUploadVisible);
		add(form);
	}
}
