package org.devocative.demeter.web.dPage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.DTaskInfo;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.task.ITaskService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.formatter.OBooleanFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.List;

public class TaskInfoDPage extends DPage {
	private static final long serialVersionUID = 3209745189144896909L;

	@Inject
	private ITaskService taskService;

	@Inject
	private ISecurityService securityService;

	public TaskInfoDPage(String id, List<String> params) {
		super(id, params);

		OColumnList<DTaskInfo> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("DTaskInfo.type"), "type"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("DTaskInfo.module"), "module"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("DTaskInfo.enabled"), "enabled")
			.setFormatter(OBooleanFormatter.bool()));

		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("entity.creationDate"), "creationDate"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("entity.modificationDate"), "modificationDate"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("entity.version"), "version"));

		columnList.add(new OAjaxLinkColumn<DTaskInfo>(new Model<String>(), DemeterIcon.EXECUTE) {
			private static final long serialVersionUID = 1953340922089825679L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<DTaskInfo> rowData) {
				DTaskInfo taskInfo = rowData.getObject();
				taskService.start(taskInfo.getId(), null, null, null);
			}
		});

		OGrid<DTaskInfo> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.setHeight(OSize.fixed(400));


		add(new WDataGrid<>("grid", oGrid, new IGridDataSource<DTaskInfo>() {
			private static final long serialVersionUID = 2797941559456313234L;

			@Override
			public List<DTaskInfo> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
				return taskService.search(pageIndex, pageSize);
			}

			@Override
			public long count() {
				return taskService.count();
			}

			@Override
			public IModel<DTaskInfo> model(DTaskInfo object) {
				return new WModel<>(object);
			}
		}).setVisible(securityService.getCurrentUser().getUsername().equals("root")) //TODO using authorization
		);
	}
}
