package org.devocative.demeter.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.devocative.demeter.core.DbDiffVO;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.core.StepResultVO;

import java.util.List;

public class StartupHandlerPage extends WebPage {
	private static final long serialVersionUID = -283332983407600664L;

	public StartupHandlerPage(PageParameters pageParameters) {
		StepResultVO latestStat = DemeterCore.getLatestStat();
		List<DbDiffVO> dbDiffs = DemeterCore.getDbDiffs();

		add(new Label("step", latestStat.getStep()));
		add(new Label("error", latestStat.getError().getMessage()));

		Form<Void> form = new Form<>("form");
		form.setVisible(!dbDiffs.isEmpty());
		form.add(new ListView<DbDiffVO>("diffs", dbDiffs) {
			private static final long serialVersionUID = 2909592281545402814L;

			@Override
			protected void populateItem(ListItem<DbDiffVO> item) {
				DbDiffVO diffVO = item.getModelObject();
				item.add(new Label("module", diffVO.getModule()));
				item.add(new Label("version", diffVO.getVersion()));
				item.add(new Label("file", diffVO.getFile()));
				item.add(new TextArea<>("sql", new PropertyModel<>(diffVO, "sql")));
			}
		});
		form.add(new Button("apply") {
			private static final long serialVersionUID = -2417464048199954007L;

			@Override
			public void onSubmit() {
				DemeterCore.applyDbDiffs(dbDiffs);
				DemeterCore.resume();
				setResponsePage(Index.class, pageParameters);
			}
		});
		add(form);
	}
}
