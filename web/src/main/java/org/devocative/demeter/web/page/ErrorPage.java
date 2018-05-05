package org.devocative.demeter.web.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.devocative.wickomp.IExceptionToMessageHandler;
import org.devocative.wickomp.WDefaults;

public class ErrorPage extends WebPage {
	public ErrorPage(Exception e) {
		final IExceptionToMessageHandler handler = WDefaults.getExceptionToMessageHandler();
		add(new Label("error", handler.handleMessage(this, e)));
	}
}
