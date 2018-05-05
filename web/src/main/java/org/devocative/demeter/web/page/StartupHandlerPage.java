package org.devocative.demeter.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.devocative.demeter.core.DbDiffVO;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.core.EStartupStep;
import org.devocative.demeter.core.StepResultVO;
import org.devocative.demeter.web.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class StartupHandlerPage extends WebPage {
	private static final long serialVersionUID = -283332983407600664L;

	private static final Logger logger = LoggerFactory.getLogger(StartupHandlerPage.class);

	private int retry = 0;
	private StepResultVO latestStat;

	public StartupHandlerPage(PageParameters pageParameters) {
		latestStat = DemeterCore.get().getLatestStat();

		final List<DbDiffVO> dbDiffs = new ArrayList<>();
		if (latestStat.getStep() == EStartupStep.Database) {
			try {
				dbDiffs.addAll(DemeterCore.get().getDbDiffs());
			} catch (Exception e) {
				logger.warn("StartupHandlerPage: DemeterCore.get().getDbDiffs(): {}", e.getMessage());
			}
		}

		add(new Label("step", new PropertyModel<>(latestStat, "step")));
		add(new Label("error", new PropertyModel<>(latestStat, "error")));
		add(new Label("retry", new PropertyModel<>(this, "retry")));

		Form<Void> form = new Form<>("form");
		//form.setVisible(!dbDiffs.isEmpty());
		form.add(new ListView<DbDiffVO>("diffs", dbDiffs) {
			private static final long serialVersionUID = 2909592281545402814L;

			@Override
			protected void populateItem(ListItem<DbDiffVO> item) {
				DbDiffVO diffVO = item.getModelObject();
				item.add(new Label("module", diffVO.getModule()));
				item.add(new Label("version", diffVO.getVersion()));
				item.add(new Label("file", diffVO.getFile()));
				item.add(new TextArea<>("sql", new Model<>(diffVO.getSql())));
			}
		});

		form.add(new Button("apply") {
			private static final long serialVersionUID = -2417464048199954007L;

			@Override
			public void onSubmit() {
				DemeterCore.get().applyDbDiffs(dbDiffs);
				DemeterCore.get().resume();
				setResponsePage(Index.class, pageParameters);
			}
		}.setVisible(!dbDiffs.isEmpty()));

		form.add(new Link("resume") {
			private static final long serialVersionUID = 7843257940731113723L;

			@Override
			public void onClick() {
				DemeterCore.get().resume();
				latestStat = DemeterCore.get().getLatestStat();
				retry++;

				if (latestStat.getStep() == EStartupStep.End) {
					setResponsePage(Index.class, pageParameters);
				}
			}
		});
		add(form);
	}
}
