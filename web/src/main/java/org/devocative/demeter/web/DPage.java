package org.devocative.demeter.web;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.devocative.demeter.iservice.IPageService;

import javax.inject.Inject;
import java.util.List;

public abstract class DPage extends Panel {
	@Inject
	private IPageService pageService;

	public DPage(String id, List<String> params) {
		super(id);
	}

	protected void redirectToDPage(Class<? extends DPage> dPageClass) {
		throw new RedirectToUrlException(getUriForPage(dPageClass));
	}

	protected String getUriForPage(Class<? extends DPage> dPageClass) {
		String uriByPage = pageService.getUriByPage(dPageClass);
		if (uriByPage.length() > 0 && uriByPage.charAt(0) == '/') {
			uriByPage = uriByPage.substring(1);
		}
		return UrlUtils.rewriteToContextRelative(uriByPage, RequestCycle.get());
	}
}
