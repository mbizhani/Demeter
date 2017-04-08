//overwrite
package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.iservice.IRoleService;
import org.devocative.demeter.vo.filter.RoleFVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.wickomp.WModel;
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
import org.devocative.wickomp.html.WAjaxLink;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RoleListDPage extends DPage implements IGridDataSource<Role> {
	private static final long serialVersionUID = 2134885276L;

	@Inject
	private IRoleService roleService;

	private RoleFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<Role> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public RoleListDPage(String id) {
		this(id, Collections.<String>emptyList(), new RoleFVO());
	}

	// Panel Call - Open Filter
	public RoleListDPage(String id, RoleFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public RoleListDPage(String id, List<String> params) {
		this(id, params, new RoleFVO());
	}

	// Main Constructor
	private RoleListDPage(String id, List<String> params, RoleFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		add(window);

		add(new WAjaxLink("add", DemeterIcon.ADD) {
			private static final long serialVersionUID = -398514209L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new RoleFormDPage(window.getContentId()));
				window.show(target);
			}
		});

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("name")
			.setLabel(new ResourceModel("Role.name")));
		floatTable.add(new WBooleanInput("dynamic")
			.setLabel(new ResourceModel("Role.dynamic")));
		floatTable.add(new WSelectionInput("permissions", roleService.getPermissionsList(), true)
			.setLabel(new ResourceModel("Role.permissions")));
		floatTable.add(new WSelectionInput("denials", roleService.getDenialsList(), true)
			.setLabel(new ResourceModel("Role.denials")));
		floatTable.add(new WSelectionInput("rowMod", ERowMod.list(), true)
			.setLabel(new ResourceModel("entity.rowMod"))
			.setVisible(getCurrentUser().isRoot()));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", roleService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", roleService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<RoleFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), DemeterIcon.SEARCH) {
			private static final long serialVersionUID = -1212746364L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<Role> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("Role.name"), "name"));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("Role.dynamic"), "dynamic")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("Role.permissions"), "permissions"));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("Role.denials"), "denials"));
		if (getCurrentUser().isRoot()) {
			columnList.add(new OPropertyColumn<Role>(new ResourceModel("entity.rowMod"), "rowMod"));
		}
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<Role>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		columnList.add(new OEditAjaxColumn<Role>() {
			private static final long serialVersionUID = -1491493091L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<Role> rowData) {
				window.setContent(new RoleFormDPage(window.getContentId(), rowData.getObject()));
				window.show(target);
			}
		});

		OGrid<Role> oGrid = new OGrid<>();
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

	public RoleListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public RoleListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public RoleListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public RoleListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public RoleListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public RoleListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public RoleListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<Role> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return roleService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return roleService.count(filter);
	}

	@Override
	public IModel<Role> model(Role object) {
		return new WModel<>(object);
	}
}