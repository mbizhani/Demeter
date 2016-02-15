package org.devocative.demeter.web;

import org.apache.wicket.request.cycle.RequestCycle;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.IPageService;

public class UrlUtil {
	private static IPageService pageService;

	public static String createBaseUri(Class<? extends DPage> dPageClass) {
		String href = getPageService().getUriByPage(dPageClass);

		return RequestCycle.get().getRequest().getContextPath() +
			DemeterWebApplication.get().getInnerContext() +
			href;
	}

	private static IPageService getPageService() {
		if (pageService == null) {
			pageService = ModuleLoader.getApplicationContext().getBean(IPageService.class);
		}
		return pageService;
	}
}
