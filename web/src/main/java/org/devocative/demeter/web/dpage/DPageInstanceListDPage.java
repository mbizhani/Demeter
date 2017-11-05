package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.DemeterPrivilegeKey;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.vo.filter.DPageInstanceFVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.demeter.web.model.DEntityLazyLoadModel;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.formatter.OBooleanFormatter;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.formatter.ONumberFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OHorizontalAlign;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class DPageInstanceListDPage extends DPage implements IGridDataSource<DPageInstance> {
	private static final long serialVersionUID = -1810838160L;

	@Inject
	private IDPageInstanceService dPageInstanceService;

	private DPageInstanceFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<DPageInstance> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public DPageInstanceListDPage(String id) {
		this(id, Collections.<String>emptyList(), new DPageInstanceFVO());
	}

	// Panel Call - Open Filter
	public DPageInstanceListDPage(String id, DPageInstanceFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public DPageInstanceListDPage(String id, List<String> params) {
		this(id, params, new DPageInstanceFVO());
	}

	// Main Constructor
	private DPageInstanceListDPage(String id, List<String> params, DPageInstanceFVO filter) {
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
		floatTable.add(new WTextInput("title")
			.setLabel(new ResourceModel("DPageInstance.title")));
		floatTable.add(new WTextInput("uri")
			.setLabel(new ResourceModel("DPageInstance.uri")));
		floatTable.add(new WBooleanInput("inMenu")
			.setLabel(new ResourceModel("DPageInstance.inMenu")));
		floatTable.add(new WTextInput("refId")
			.setLabel(new ResourceModel("DPageInstance.refId")));
		floatTable.add(new WSelectionInput("pageInfo", dPageInstanceService.getPageInfoList(), true)
			.setLabel(new ResourceModel("DPageInstance.pageInfo")));
		floatTable.add(new WSelectionInput("roles", dPageInstanceService.getRolesList(), true)
			.setLabel(new ResourceModel("DPageInstance.roles")));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", dPageInstanceService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", dPageInstanceService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<DPageInstanceFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), DemeterIcon.SEARCH) {
			private static final long serialVersionUID = 985915736L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<DPageInstance> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("DPageInstance.title"), "title"));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("DPageInstance.uri"), "uri")
			.setAlign(OHorizontalAlign.Left)
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("DPageInstance.inMenu"), "inMenu")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<>(new ResourceModel("DPageInstance.refId"), "refId"));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("DPageInstance.pageInfo"), "pageInfo")
			.setStyle("direction:ltr;text-align:left"));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("DPageInstance.roles"), "roles")
			.setWidth(OSize.fixed(150)));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<DPageInstance>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(DemeterPrivilegeKey.DPageInstEdit)) {
			columnList.add(new OEditAjaxColumn<DPageInstance>() {
				private static final long serialVersionUID = -1083932431L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<DPageInstance> rowData) {
					window.setContent(new DPageInstanceFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		OGrid<DPageInstance> oGrid = new OGrid<>();
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

	public DPageInstanceListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public DPageInstanceListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public DPageInstanceListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public DPageInstanceListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public DPageInstanceListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public DPageInstanceListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public DPageInstanceListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<DPageInstance> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return dPageInstanceService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return dPageInstanceService.count(filter);
	}

	@Override
	public IModel<DPageInstance> model(DPageInstance object) {
		return new DEntityLazyLoadModel<>(object.getId(), dPageInstanceService);
	}
}