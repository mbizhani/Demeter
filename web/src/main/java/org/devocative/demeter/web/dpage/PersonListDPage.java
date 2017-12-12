//overwrite
package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.DemeterPrivilegeKey;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.Person;
import org.devocative.demeter.iservice.IPersonService;
import org.devocative.demeter.vo.filter.PersonFVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.demeter.web.component.grid.OEditAjaxColumn;
import org.devocative.demeter.web.component.grid.ORowModChangeAjaxColumn;
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

public class PersonListDPage extends DPage implements IGridDataSource<Person> {
	private static final long serialVersionUID = 812634205L;

	@Inject
	private IPersonService personService;

	private PersonFVO filter;
	private boolean formVisible = true;
	private String[] invisibleFormItems;

	private WDataGrid<Person> grid;
	private String[] removeColumns;

	private Boolean gridFit;
	private boolean gridEnabled = false;
	private OSize gridHeight = OSize.fixed(500);
	private OSize gridWidth = OSize.percent(100);

	// ------------------------------

	// Panel Call - New Filter
	public PersonListDPage(String id) {
		this(id, Collections.<String>emptyList(), new PersonFVO());
	}

	// Panel Call - Open Filter
	public PersonListDPage(String id, PersonFVO filter) {
		this(id, Collections.<String>emptyList(), filter);
	}

	// REST Call - New Filter
	public PersonListDPage(String id, List<String> params) {
		this(id, params, new PersonFVO());
	}

	// Main Constructor
	private PersonListDPage(String id, List<String> params, PersonFVO filter) {
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
			private static final long serialVersionUID = -700778656L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new PersonFormDPage(window.getContentId()));
				window.show(target);
			}
		}.setVisible(hasPermission(DemeterPrivilegeKey.PersonAdd)));

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("firstName")
			.setLabel(new ResourceModel("Person.firstName")));
		floatTable.add(new WTextInput("lastName")
			.setLabel(new ResourceModel("Person.lastName")));
		floatTable.add(new WDateRangeInput("birthRegDate")
			.setTimePartVisible(false)
			.setLabel(new ResourceModel("Person.birthRegDate")));
		floatTable.add(new WTextInput("email")
			.setLabel(new ResourceModel("Person.email")));
		floatTable.add(new WTextInput("mobile")
			.setLabel(new ResourceModel("Person.mobile")));
		floatTable.add(new WTextInput("systemNumber")
			.setLabel(new ResourceModel("Person.systemNumber")));
		floatTable.add(new WBooleanInput("hasUser")
			.setLabel(new ResourceModel("Person.hasUser")));
		floatTable.add(new WSelectionInput("rowMod", ERowMod.list(), true)
			.setLabel(new ResourceModel("entity.rowMod"))
			.setVisible(getCurrentUser().isRoot()));
		floatTable.add(new WDateRangeInput("creationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.creationDate")));
		floatTable.add(new WSelectionInput("creatorUser", personService.getCreatorUserList(), true)
			.setLabel(new ResourceModel("entity.creatorUser")));
		floatTable.add(new WDateRangeInput("modificationDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("entity.modificationDate")));
		floatTable.add(new WSelectionInput("modifierUser", personService.getModifierUserList(), true)
			.setLabel(new ResourceModel("entity.modifierUser")));

		Form<PersonFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), DemeterIcon.SEARCH) {
			private static final long serialVersionUID = 368896197L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				grid.setEnabled(true);
				grid.loadData(target);
			}
		});
		add(form);

		OColumnList<Person> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("Person.firstName"), "firstName"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("Person.lastName"), "lastName"));
		columnList.add(new OPropertyColumn<Person>(new ResourceModel("Person.birthRegDate"), "birthRegDate")
			.setFormatter(ODateFormatter.getDateByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("Person.email"), "email"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("Person.mobile"), "mobile"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("Person.systemNumber"), "systemNumber"));
		columnList.add(new OPropertyColumn<Person>(new ResourceModel("Person.hasUser"), "hasUser")
			.setFormatter(OBooleanFormatter.bool()));
		columnList.add(new OPropertyColumn<>(new ResourceModel("Person.user"), "user"));
		if (getCurrentUser().isRoot()) {
			columnList.add(new OPropertyColumn<>(new ResourceModel("entity.rowMod"), "rowMod"));
		}
		columnList.add(new OPropertyColumn<Person>(new ResourceModel("entity.creationDate"), "creationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<Person>(new ResourceModel("entity.modificationDate"), "modificationDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<Person>(new ResourceModel("entity.version"), "version")
			.setFormatter(ONumberFormatter.integer())
			.setStyle("direction:ltr"));

		if (hasPermission(DemeterPrivilegeKey.PersonEdit)) {
			columnList.add(new OEditAjaxColumn<Person>() {
				private static final long serialVersionUID = -1094579746L;

				@Override
				public void onClick(AjaxRequestTarget target, IModel<Person> rowData) {
					window.setContent(new PersonFormDPage(window.getContentId(), rowData.getObject()));
					window.show(target);
				}
			});

			columnList.add(new ORowModChangeAjaxColumn<>(window));
		}

		OGrid<Person> oGrid = new OGrid<>();
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

	public PersonListDPage setFormVisible(boolean formVisible) {
		this.formVisible = formVisible;
		return this;
	}

	public PersonListDPage setInvisibleFormItems(String... invisibleFormItems) {
		this.invisibleFormItems = invisibleFormItems;
		return this;
	}

	public PersonListDPage setGridHeight(OSize gridHeight) {
		this.gridHeight = gridHeight;
		return this;
	}

	public PersonListDPage setGridWidth(OSize gridWidth) {
		this.gridWidth = gridWidth;
		return this;
	}

	public PersonListDPage setGridFit(Boolean gridFit) {
		this.gridFit = gridFit;
		return this;
	}

	public PersonListDPage setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		return this;
	}

	public PersonListDPage setRemoveColumns(String... removeColumns) {
		this.removeColumns = removeColumns;
		return this;
	}

	// ------------------------------ IGridDataSource

	@Override
	public List<Person> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
		return personService.search(filter, pageIndex, pageSize);
	}

	@Override
	public long count() {
		return personService.count(filter);
	}

	@Override
	public IModel<Person> model(Person object) {
		return new WModel<>(object);
	}
}