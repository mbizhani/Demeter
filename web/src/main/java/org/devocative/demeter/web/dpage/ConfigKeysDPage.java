package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.IConfigKey;
import org.devocative.adroit.vo.KeyValueVO;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WSelectionInput;
import org.devocative.wickomp.form.WTextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

public class ConfigKeysDPage extends DPage {
	private static final long serialVersionUID = 599845806014357307L;

	private static final Logger logger = LoggerFactory.getLogger(ConfigKeysDPage.class);

	private Map<String, String> keysValue = new LinkedHashMap<>();
	private Map<String, String> remappedKeys = new HashMap<>();
	private Map<String, String> inverseRemappedKeys = new HashMap<>();

	@Inject
	private ISecurityService securityService;

	public ConfigKeysDPage(String id, List<String> params) {
		super(id, params);

		fillConfigKeys();

		Form<Map<String, String>> form = new Form<>("form", new CompoundPropertyModel<>(keysValue));
		form.setVisible(securityService.getCurrentUser().getUsername().equals("root"));
		add(form);

		form.add(new ListView<IConfigKey>("keys", ConfigUtil.getConfigKeys()) {
			private static final long serialVersionUID = 7292528901486002146L;

			@Override
			protected void populateItem(ListItem<IConfigKey> item) {
				IConfigKey configKey = item.getModelObject();

				item.add(new Label("label", configKey.getKey()));

				String remapped = remappedKeys.get(configKey.getKey());
				RepeatingView key = new RepeatingView("key");
				if (configKey.getPossibleValues() == null) {
					key.add(new WTextInput(remapped));
				} else {
					List<String> pValues = new ArrayList<>();
					for (Object pv : configKey.getPossibleValues()) {
						pValues.add(pv.toString());
					}
					key.add(new WSelectionInput(remapped, pValues, false));
				}
				item.add(key);
			}
		});

		form.add(new DAjaxButton("update") {
			private static final long serialVersionUID = 8326667705742791278L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				logger.warn("Update config values: User=[{}]", securityService.getCurrentUser());

				for (Map.Entry<String, String> entry : keysValue.entrySet()) {
					String key = inverseRemappedKeys.get(entry.getKey());
					ConfigUtil.updateKey(key, entry.getValue());
				}
			}
		});


		add(new ListView<KeyValueVO<Object, Object>>("properties", KeyValueVO.fromMap(System.getProperties())) {
			private static final long serialVersionUID = -157046053543529923L;

			@Override
			protected void populateItem(ListItem<KeyValueVO<Object, Object>> item) {
				KeyValueVO<Object, Object> keyValueVO = item.getModelObject();

				item.add(new Label("key", keyValueVO.getKey().toString()));
				item.add(new Label("value", keyValueVO.getValue().toString()));
			}
		});
	}

	private void fillConfigKeys() {
		int idx = 1;
		for (IConfigKey configKey : ConfigUtil.getConfigKeys()) {
			String def = "";
			if (configKey.getDefaultValue() != null) {
				def = configKey.getDefaultValue().toString();
			}
			String remapped = "f" + idx++;
			remappedKeys.put(configKey.getKey(), remapped);
			inverseRemappedKeys.put(remapped, configKey.getKey());
			keysValue.put(remapped, ConfigUtil.getString(configKey.getKey(), def));
		}
	}
}
