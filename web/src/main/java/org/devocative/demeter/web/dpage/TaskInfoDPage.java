package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.DTaskInfo;
import org.devocative.demeter.entity.DTaskState;
import org.devocative.demeter.iservice.task.ITaskService;
import org.devocative.demeter.vo.DTaskVO;
import org.devocative.demeter.vo.filter.DTaskFVO;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DTaskBehavior;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.WModel;
import org.devocative.wickomp.WebUtil;
import org.devocative.wickomp.async.IAsyncResponse;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.form.range.WDateRangeInput;
import org.devocative.wickomp.formatter.OBooleanFormatter;
import org.devocative.wickomp.formatter.ODateFormatter;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;
import org.devocative.wickomp.grid.WSortField;
import org.devocative.wickomp.grid.column.OColumnList;
import org.devocative.wickomp.grid.column.OPropertyColumn;
import org.devocative.wickomp.grid.column.link.OAjaxLinkColumn;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.opt.OSize;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class TaskInfoDPage extends DPage implements IAsyncResponse<Object> {
	private static final long serialVersionUID = 3209745189144896909L;

	private DTaskFVO filter = new DTaskFVO();

	private WDataGrid<DTaskVO> running;
	private DTaskBehavior<Object> taskBehavior;
	private WebMarkupContainer log;

	@Inject
	private ITaskService taskService;

	// ------------------------------

	public TaskInfoDPage(String id, List<String> params) {
		super(id, params);
	}

	// ------------------------------

	@Override
	public void onAsyncResult(IPartialPageRequestHandler handler, Object result) {
		/*String str = Strings.escapeMarkup(result.toString(), false, true).toString();
		str = str.replaceAll("[\n]", "<br/>");
		str = str.replaceAll("[\r]", "");*/

		String script = String.format("$('#%s').append(\"<div><span style='color:blue'>%s | </span>%s</div>\");",
			log.getMarkupId(),
			Thread.currentThread().getName(),
			WebUtil.escape(result.toString(), false, true));

		handler.appendJavaScript(script);
		handler.appendJavaScript(String.format("$('#%1$s').scrollTop($('#%1$s')[0].scrollHeight);", log.getMarkupId()));
	}

	@Override
	public void onAsyncError(IPartialPageRequestHandler handler, Exception e) {
		String script = String.format("$('#%s').append(\"<div><span style='color:red'>%s | </span>%s</div>\");",
			log.getMarkupId(),
			Thread.currentThread().getName(),
			e.toString());
		handler.appendJavaScript(script);
		handler.appendJavaScript(String.format("$('#%1$s').scrollTop($('#%1$s')[0].scrollHeight);", log.getMarkupId()));
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		taskBehavior = new DTaskBehavior<>(this);
		add(taskBehavior);

		log = new WebMarkupContainer("log");
		log.setOutputMarkupId(true);
		add(log);

		initDTaskInfoList();

		// ---------------

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.add(new WTextInput("id")
			.setLabel(new ResourceModel("DTaskVO.id")));
		floatTable.add(new WTextInput("type")
			.setLabel(new ResourceModel("DTaskVO.type")));
		floatTable.add(new WDateRangeInput("startDate")
			.setTimePartVisible(true)
			.setLabel(new ResourceModel("DTaskVO.startDate")));
		floatTable.add(new WSelectionInput("state", Arrays.asList(DTaskState.values()), true)
			.setLabel(new ResourceModel("DTaskVO.state")));
		floatTable.add(new WTextInput("currentUser")
			.setLabel(new ResourceModel("DTaskVO.currentUser")));

		Form<DTaskFVO> form = new Form<>("form", new CompoundPropertyModel<>(filter));
		form.add(floatTable);
		form.add(new DAjaxButton("search", new ResourceModel("label.search"), DemeterIcon.SEARCH) {
			private static final long serialVersionUID = 29188431L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				running.setEnabled(true);
				running.loadData(target);
			}
		});
		add(form);

		OColumnList<DTaskVO> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("DTaskVO.id"), "id"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("DTaskVO.type"), "type"));
		columnList.add(new OPropertyColumn<DTaskVO>(new ResourceModel("DTaskVO.startDate"), "startDate")
			.setFormatter(ODateFormatter.getDateTimeByUserPreference())
			.setStyle("direction:ltr"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("DTaskVO.state"), "state"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("DTaskVO.currentUser"), "currentUser"));
		columnList.add(new OAjaxLinkColumn<DTaskVO>(new Model<>(), DemeterIcon.STOP_CIRCLE) {
			private static final long serialVersionUID = 1830759784L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<DTaskVO> rowData) {
				String key = rowData.getObject().getKey();
				taskService.stop(key);
			}
		}.setConfirmMessage(getString("label.confirm")));
		columnList.add(new OAjaxLinkColumn<DTaskVO>(new Model<>(), DemeterIcon.DOT_CIRCLE_O) {
			private static final long serialVersionUID = 1830759784L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<DTaskVO> rowData) {
				String key = rowData.getObject().getKey();
				taskService.attachToCallback(key, taskBehavior);
			}
		});
		columnList.add(new OAjaxLinkColumn<DTaskVO>(new Model<>(), DemeterIcon.CIRCLE_O) {
			private static final long serialVersionUID = 1830759784L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<DTaskVO> rowData) {
				String key = rowData.getObject().getKey();
				taskService.detachFromCallback(key, taskBehavior);
			}
		});

		OGrid<DTaskVO> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.setHeight(OSize.fixed(300))
			.setWidth(OSize.percent(100))
		;

		running = new WDataGrid<>("running", oGrid, new IGridDataSource<DTaskVO>() {
			private static final long serialVersionUID = 8350761390873907281L;

			@Override
			public List<DTaskVO> list(long pageIndex, long pageSize, List<WSortField> sortFields) {
				return taskService.search(filter, pageIndex, pageSize);
			}

			@Override
			public long count() {
				return taskService.count(filter);
			}

			@Override
			public IModel<DTaskVO> model(DTaskVO object) {
				return new WModel<>(object);
			}
		});
		running.setEnabled(false);
		add(running);
	}

	// ------------------------------

	private void initDTaskInfoList() {
		OColumnList<DTaskInfo> columnList = new OColumnList<>();
		columnList.add(new OPropertyColumn<>(new ResourceModel("DTaskInfo.type"), "type"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("DTaskInfo.module"), "module"));
		columnList.add(new OPropertyColumn<DTaskInfo>(new ResourceModel("DTaskInfo.enabled"), "enabled")
			.setFormatter(OBooleanFormatter.bool()));

		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creationDate"), "creationDate"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.creatorUser"), "creatorUser"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modificationDate"), "modificationDate"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.modifierUser"), "modifierUser"));
		columnList.add(new OPropertyColumn<>(new ResourceModel("entity.version"), "version"));

		columnList.add(new OAjaxLinkColumn<DTaskInfo>(new Model<>(), DemeterIcon.EXECUTE) {
			private static final long serialVersionUID = 1953340922089825679L;

			@Override
			public void onClick(AjaxRequestTarget target, IModel<DTaskInfo> rowData) {
				DTaskInfo taskInfo = rowData.getObject();
				taskService.start(taskInfo.getId(), null, null, null);
			}
		}.setConfirmMessage(getString("label.confirm")));

		OGrid<DTaskInfo> oGrid = new OGrid<>();
		oGrid
			.setColumns(columnList)
			.setMultiSort(false)
			.setHeight(OSize.fixed(300));


		add(new WDataGrid<>("tasks", oGrid, new IGridDataSource<DTaskInfo>() {
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
		}));
	}
}
