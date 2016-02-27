package org.devocative.demeter.web;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IPageService;

public class UrlUtil {
	private static IPageService pageService;

	public static void redirectTo(Class<? extends DPage> dPageClass) {
		redirectTo(dPageClass, null);
	}

	public static void redirectTo(Class<? extends DPage> dPageClass, String ref) {
		String uri = createUri(dPageClass, false);
		if (ref != null) {
			if (ref.startsWith("/")) {
				uri += ref;
			} else {
				uri += "/" + ref;
			}
		}
		RequestCycle.get().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(uri));
	}

	public static String createUri(Class<? extends DPage> dPageClass, boolean needAbsolute) {
		String href = getPageService().getUriByPage(dPageClass);

		return createUri(href, needAbsolute);
	}

	public static String createUri(DPageInstance pageInstance, boolean needAbsolute) {
		return createUri(pageInstance.getUri(), needAbsolute);
	}

	public static String createUri(String href, boolean needAbsolute) {
		String result = "";

		if (needAbsolute) {
			result = RequestCycle.get().getRequest().getContextPath();
		}

		result += DemeterWebApplication.get().getInnerContext() + href;

		return result;
	}

	private static IPageService getPageService() {
		if (pageService == null) {
			pageService = ModuleLoader.getApplicationContext().getBean(IPageService.class);
		}
		return pageService;
	}
}
