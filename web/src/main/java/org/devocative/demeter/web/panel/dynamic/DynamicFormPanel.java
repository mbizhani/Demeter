package org.devocative.demeter.web.panel.dynamic;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.devocative.demeter.web.DPanel;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.html.WFloatTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DynamicFormPanel extends DPanel {
	private static final long serialVersionUID = -5981609058208439713L;

	private List<FormComponent> components = new ArrayList<>();
	private List<SubmitButtonInfo> buttons = new ArrayList<>();

	// ------------------------------

	public DynamicFormPanel(String id) {
		super(id);
	}

	// ------------------------------

	public abstract void onSubmit(AjaxRequestTarget target, Map<String, Object> formData, String buttonName);

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<Map<String, Object>> form = new Form<>("form", new CompoundPropertyModel<>(new HashMap<>()));
		add(form);

		WFloatTable floatTable = new WFloatTable("floatTable");
		form.add(floatTable);

		floatTable.add(new ListView<FormComponent>("fields", components) {
			private static final long serialVersionUID = 4235549921764383353L;

			@Override
			protected void populateItem(ListItem<FormComponent> item) {
				RepeatingView view = new RepeatingView("field");
				view.add(item.getModelObject());
				item.add(view);
			}
		});

		form.add(new ListView<SubmitButtonInfo>("buttons", buttons) {
			private static final long serialVersionUID = 2438186359303026031L;

			@Override
			protected void populateItem(ListItem<SubmitButtonInfo> item) {
				SubmitButtonInfo info = item.getModelObject();

				item.add(new DAjaxButton("button", info.getCaption(), info.getIcon()) {
					private static final long serialVersionUID = 7122375666071013641L;

					@Override
					protected void onSubmit(AjaxRequestTarget target) {
						DynamicFormPanel.this.onSubmit(target, form.getModelObject(), info.getName());
					}
				});
			}
		});
	}

	protected final DynamicFormPanel addFormComponent(FormComponent component) {
		components.add(component);
		return this;
	}

	protected final DynamicFormPanel addButton(SubmitButtonInfo info) {
		buttons.add(info);
		return this;
	}
}
