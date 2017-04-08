package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.DemeterPrivilegeKey;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.IPersonService;
import org.devocative.demeter.iservice.IUserService;
import org.devocative.demeter.vo.filter.UserFVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.form.WBooleanInput;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.form.range.WNumberRangeInput;
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

public class UserListDPage extends DPage implements IGridDataSource<User> {
	private static final long serialVersionUID = -1089910809L;

	@Inject
	private IUserService userService;

	@Inject
	private IPersonService personService;

	private UserFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<User> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public UserListDPage(String id) {
		this(id, Collections.<String>emptyList(), new UserFVO());
	}

	// Panel Call - Open Filter
	public UserListDPage(String id, UserFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public UserListDPage(String id, List<String> params) {
		this(id, params, new UserFVO());
	}

	// Main Constructor
	private UserListDPage(String id, List<String> params, UserFVO filter) {
		super(id, params);

		this.filter = filter;
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final WModalWindow window = new WModalWindow("window");
		window.getOptions().setWidth(OSize.fixed(650));
		add(window);

		add(new WAjaxLink("add", DemeterIcon.ADD) {
			private static final long serialVersionUID = 1274284650L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new UserFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(DemeterPrivilegeKey.UserAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("username")
			.setLabel(new ResourceModel("User.username")));
		floatTable.add(new WTextInput("person.firstName")
			.setLabel(new ResourceModel("Person.firstName")));
		floatTable.add(new WTextInput("person.lastName")
			.setLabel(new ResourceModel("Person.lastName")));
		floatTable.add(new WSelectionInput("authMechanism", EAuthMechanism.list(), true)
			.setLabel(new ResourceModel("User.authMechanism")));
		floatTable.add(new WSelectionInput("status", EUserStatus.list(), true)
			.setLabel(new ResourceModel("User.status")));
		floatTable.add(new WSelectionInput("locale", ELocale.list(), true)
			.setLabel(new ResourceModel("User.locale")));
		floatTable.add(new WSelectionInput("calendarType", ECalendar.list(), true)
			.setLabel(new ResourceModel("User.calendarType")));
		floatTable.add(new WSelectionInput("layoutDirectionType", ELayoutDirection.list(), true)
			.setLabel(new ResourceModel("User.layoutDirectionType")));
		floatTable.add(new WSelectionInput("datePatternType", EDatePatternType.list(), true)
			.setLabel(new ResourceModel("User.datePatternType")));
		floatTable.add(new WSelectionInput("dateTimePatternType", EDateTimePatternType.list(), true)
			.setLabel(new ResourceModel("User.dateTimePatternType")));
		floatTable.add(new WDateRangeInput("lastLoginDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("User.lastLoginDate")));
		floatTable.add(new WBooleanInput("admin")
			.setLabel(new ResourceModel("User.admin")));
		floatTable.add(new WNumberRangeInput("sessionTimeout", Integer.class)
			.setLabel(new ResourceModel("User.sessionTimeout")));
		floatTable.add(new WSelectionInput("roles", userService.getRolesList(), true)
			.setLabel(new ResourceModel("User.roles")));
		floatTable.add(new WSelectionInput("authorizations", userService.getAuthorizationsList(), true)
			.setLabel(new ResourceModel("User.authorizations")));

		floatTable.add(new WSelectionInput("person.rowMod", ERowMod.list(), true)
			.setLabel(new ResourceModel("entity.rowMod"))
			.setVisible(getCurrentUser().isRoot()));
		floatTable.add(new WDateRangeInput("person.creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("person.creatorUser", personService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("person.modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("person.modifierUser", personService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<UserFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), DemeterIcon.SEARCH) {
			private static final long serialVersionUID = 29188431L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<User> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.username"), "username"));

		// -- Person
		columnList.add(new OPropertyColumn<User>(new ResourceModel("Person.firstName"), "person.firstName"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("Person.lastName"), "person.lastName"));

		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.authMechanism"), "authMechanism"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.status"), "status"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.locale"), "locale"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.calendarType"), "calendarType"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.layoutDirectionType"), "layoutDirectionType"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.datePatternType"), "datePatternType"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.dateTimePatternType"), "dateTimePatternType"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.lastLoginDate"), "lastLoginDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.admin"), "admin")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.sessionTimeout"), "sessionTimeout")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.roles"), "roles"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("User.authorizations"), "authorizations"));

		// -- Person
		if (getCurrentUser().isRoot()) {
			columnList.add(new OPropertyColumn<User>(new ResourceModel("entity.rowMod"), "person.rowMod"));
		}
		columnList.add(new OPropertyColumn<User>(new ResourceModel("entity.creationDate"), "person.creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("entity.creatorUser"), "person.creatorUser"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("entity.modificationDate"), "person.modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("entity.modifierUser"), "person.modifierUser"));
		columnList.add(new OPropertyColumn<User>(new ResourceModel("entity.version"), "person.version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(DemeterPrivilegeKey.UserEdit)) {
			columnList.add(new OEditAjaxColumn<User>() {
				private static final long serialVersionUID = 1830759784L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<User> rowData) {
					window.setContent(new UserFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});
		}

		OGrid<User> oGrid = new OGrid<>();
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

	public UserListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public UserListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public UserListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public UserListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public UserListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public UserListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public UserListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<User> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return userService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return userService.count(filter);
	}

	@Override
	public IModel<User> model(User object) {
		return new WModel<>(object);
	}
}