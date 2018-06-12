package org.devocative.demeter.web.dpage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.demeter.entity.ZSqlApply;
import org.devocative.demeter.iservice.IDemeterCoreService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.web.DPage;

import javax.inject.Inject;
import java.util.List;
import java.util.TreeMap;

public class InfoDPage extends DPage {
	private static final long serialVersionUID = -5991248705396708018L;

	@Inject
	private IPersistorService persistorService;

	@Inject
	private IDemeterCoreService demeterCoreService;

	public InfoDPage(String id, List<String> params) {
		super(id, params);

		add(new Label("startUp", getCurrentUser().formatDate(
			demeterCoreService.getStartUpDate(),
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
				item.add(new Label("apply", getCurrentUser().formatDate(
					apply.getApply(),
					getCurrentUser().getDateTimePatternType().toString()
				)));
			}
		});

		add(new ListView<KeyValueVO<Object, Object>>("properties", KeyValueVO.fromMap(new TreeMap<>(System.getProperties()))) {
			private static final long serialVersionUID = -157046053543529923L;

			@Override
			protected void populateItem(ListItem<KeyValueVO<Object, Object>> item) {
				KeyValueVO<Object, Object> keyValueVO = item.getModelObject();

				item.add(new Label("key", keyValueVO.getKey().toString()));
				item.add(new Label("value", keyValueVO.getValue().toString()));
			}
		});

		add(new ListView<KeyValueVO<String, String>>("variables", KeyValueVO.fromMap(new TreeMap<>(System.getenv()))) {
			private static final long serialVersionUID = -1570460531143529923L;

			@Override
			protected void populateItem(ListItem<KeyValueVO<String, String>> item) {
				KeyValueVO<String, String> keyValueVO = item.getModelObject();

				item.add(new Label("key", keyValueVO.getKey()));
				item.add(new Label("value", keyValueVO.getValue()));
			}
		});
	}
}
