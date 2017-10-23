package org.devocative.demeter.web.dpage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.entity.ZSqlApply;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.web.DPage;

import javax.inject.Inject;
import java.util.List;

public class InfoDPage extends DPage {
	private static final long serialVersionUID = -5991248705396708018L;

	@Inject
	private IPersistorService persistorService;

	public InfoDPage(String id, List<String> params) {
		super(id, params);

		add(new Label("startUp", getCurrentUser().getCalendar().convertToString(
			DemeterCore.get().getStartUpDate(),
			getCurrentUser().getDateTimePatternType().toString()
		)));

		List<ZSqlApply> list = persistorService.list(ZSqlApply.class);
		add(new ListView<ZSqlApply>("rows", list) {
			private static final long serialVersionUID = 8103855266731515145L;

			@Override
			protected void populateItem(ListItem<ZSqlApply> item) {
				ZSqlApply apply = item.getModelObject();

				item.add(new Label("module", apply.getModule()));
				item.add(new Label("version", apply.getVersion()));
				item.add(new Label("file", apply.getFile()));
				item.add(new Label("apply", getCurrentUser().getCalendar().convertToString(
					apply.getApply(),
					getCurrentUser().getDateTimePatternType().toString()
				)));
			}
		});
	}
}
