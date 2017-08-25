package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.DemeterPrivilegeKey;
import org.devocative.demeter.entity.EFileStatus;
import org.devocative.demeter.entity.EFileStorage;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.vo.filter.FileStoreFVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.formatter.ONumberFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumn;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class FileStoreListDPage extends DPage implements IGridDataSource<FileStore> {
	private static final long serialVersionUID = 140307859L;

	@Inject
	private IFileStoreService fileStoreService;

	private FileStoreFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<FileStore> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public FileStoreListDPage(String id) {
		this(id, Collections.<String>emptyList(), new FileStoreFVO());
	}

	// Panel Call - Open Filter
	public FileStoreListDPage(String id, FileStoreFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public FileStoreListDPage(String id, List<String> params) {
		this(id, params, new FileStoreFVO());
	}

	// Main Constructor
	private FileStoreListDPage(String id, List<String> params, FileStoreFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		add(window);

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("FileStore.name")));
		floatTable.add(new WSelectionInput("status", EFileStatus.list(), true)
			.setLabel(new ResourceModel("FileStore.status")));
		floatTable.add(new WSelectionInput("storage", EFileStorage.list(), true)
			.setLabel(new ResourceModel("FileStore.storage")));
		floatTable.add(new WSelectionInput("mimeType", EMimeType.list(), true)
			.setLabel(new ResourceModel("FileStore.mimeType")));
		floatTable.add(new WTextInput("fileId")
			.setLabel(new ResourceModel("FileStore.fileId")));
		floatTable.add(new WTextInput("tag")
			.setLabel(new ResourceModel("FileStore.tag")));
		floatTable.add(new WDateRangeInput("expiration")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("FileStore.expiration")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", fileStoreService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", fileStoreService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<FileStoreFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), DemeterIcon.SEARCH) {
			private static final long serialVersionUID = -1714821893L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<FileStore> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("FileStore.name"), "name"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("FileStore.status"), "status"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("FileStore.storage"), "storage"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("FileStore.mimeType"), "mimeType"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("FileStore.fileId"), "fileId"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("FileStore.tag"), "tag"));
		columnList.add(new OPropertyColumn<FileStore>(new ResourceModel("FileStore.expiration"), "expiration")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<FileStore>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<FileStore>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<FileStore>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(DemeterPrivilegeKey.FileStoreEdit)) {
			columnList.add(new OEditAjaxColumn<FileStore>() {
				private static final long serialVersionUID = 1588198164L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<FileStore> rowData) {
					window.setContent(new FileStoreFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		columnList.add(new OColumn<FileStore>(new Model<>(), "DOWNLOAD") {
			private static final long serialVersionUID = -501065240513534269L;

			@Override
			public String cellValue(FileStore bean, String id, int colNo, String url) {
				return String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
					UrlUtil.getFileUri(bean.getFileId()), DemeterIcon.DOWNLOAD.toString());
			}

			@Override
			public String footerCellValue(Object bean, int colNo, String url) {
				return null;
			}
		});


		OGrid<FileStore> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.setHeight(gridHeight)
			.setWidth(gridWidth)
			.setFit(gridFit);

		grid = new WDataGrid<>("grid", oGrid, this);
		add(grid);

		// ---------------

		form.setVisible(formVisible);
		grid.setEnabled(gridEnabled || !formVisible);

		if (invisibleFormItems != null) {
			for (String formItem : invisibleFormItems) {
				floatTable.get(formItem).setVisible(false);
			}
		}

		if (removeColumns != null) {
			for (String column : removeColumns) {
				columnList.removeColumn(column);
			}
		}
	}

	// ------------------------------

	public FileStoreListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public FileStoreListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public FileStoreListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public FileStoreListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public FileStoreListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public FileStoreListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public FileStoreListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<FileStore> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return fileStoreService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return fileStoreService.count(filter);
	}

	@Override
	public IModel<FileStore> model(FileStore object) {
		return new WModel<>(object);
	}
}