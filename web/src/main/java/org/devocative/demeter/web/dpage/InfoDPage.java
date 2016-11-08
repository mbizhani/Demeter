package org.devocative.demeter.web.dpage;

import org.apache.wicket.markup.html.basic.Label;
import org.devocative.demeter.web.DPage;

import java.util.List;

public class InfoDPage extends DPage {
	private static final long serialVersionUID = -5991248705396708018L;

	/*@Inject
	private ITaskService taskService;*/

	public InfoDPage(String id, List<String> params) {
		super(id, params);

		add(new Label("params", params.toString()));

		/*add(new AjaxLink("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				taskService.start("dmtSimpleDTask");
			}
		});*/

		/*add(new WTextInput("text"));
		add(new WDateInput("date"));
		add(new WDateRangeInput("dateRange"));
		add(new WSelectionInput("selection", Arrays.asList("A", "B"), false));*/

		/*add(new AjaxLink("alink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				UrlUtil.redirectTo(LoginDPage.class, "asd");
			}
		});

		add(new Link("link") {
			@Override
			public void onClick() {
				UrlUtil.redirectTo(LoginDPage.class);
			}
		});*/
	}
}
