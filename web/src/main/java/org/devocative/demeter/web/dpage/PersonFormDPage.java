//overwrite
package org.devocative.demeter.web.dpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.devocative.demeter.entity.Person;
import org.devocative.demeter.iservice.IPersonService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.demeter.web.component.DAjaxButton;
import org.devocative.wickomp.form.WDateInput;
import org.devocative.wickomp.form.WTextInput;
import org.devocative.wickomp.html.WFloatTable;
import org.devocative.wickomp.html.window.WModalWindow;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class PersonFormDPage extends DPage {
	private static final long serialVersionUID = -448521225L;

	@Inject
	private IPersonService personService;

	private Person entity;

	// ------------------------------

	public PersonFormDPage(String id) {
		this(id, new Person());
	}

	// Main Constructor - For Ajax Call
	public PersonFormDPage(String id, Person entity) {
		super(id, Collections.<String>emptyList());

		this.entity = entity;
	}

	// ---------------

	// Main Constructor - For REST Call
	public PersonFormDPage(String id, List<String> params) {
		super(id, params);

		this.entity = params != null && !params.isEmpty() ?
			personService.load(Long.valueOf(params.get(0))) :
			new Person();
	}

	// ------------------------------

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WFloatTable floatTable = new WFloatTable("floatTable");
		floatTable.setEqualWidth(true);
		floatTable.add(new WTextInput("firstName")
			.setLabel(new ResourceModel("Person.firstName")));
		floatTable.add(new WTextInput("lastName")
			.setLabel(new ResourceModel("Person.lastName")));
		floatTable.add(new WDateInput("birthRegDate")
			.setTimePartVisible(false)
			.setLabel(new ResourceModel("Person.birthRegDate")));
		floatTable.add(new WTextInput("email")
			.setLabel(new ResourceModel("Person.email")));
		floatTable.add(new WTextInput("mobile")
			.setLabel(new ResourceModel("Person.mobile")));
		floatTable.add(new WTextInput("systemNumber")
			.setLabel(new ResourceModel("Person.systemNumber")));

		Form<Person> form = new Form<>("form", new CompoundPropertyModel<>(entity));
		form.add(floatTable);

		form.add(new DAjaxButton("save", new ResourceModel("label.save"), DemeterIcon.SAVE) {
			private static final long serialVersionUID = 176856415L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				personService.saveOrUpdate(entity);

				if (!WModalWindow.closeParentWindow(PersonFormDPage.this, target)) {
					UrlUtil.redirectTo(PersonListDPage.class);
				}
			}
		});
		add(form);
	}
}