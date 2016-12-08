package org.devocative.demeter.web;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IPageService;

import java.util.List;

public class UrlUtil {
	private static IPageService pageService;

	public static void redirectTo(Class<? extends DPage> dPageClass, List<String> restParams) {
		String[] restParamsArr = null;
		if (restParams != null && restParams.size() > 0) {
			restParamsArr = restParams.toArray(new String[restParams.size()]);
		}
		redirectTo(dPageClass, restParamsArr);
	}

	// Main Method
	public static void redirectTo(Class<? extends DPage> dPageClass, String... restParams) {
		String uri = createUri(dPageClass, false);
		if (restParams != null) {
			for (String param : restParams) {
				if (param.startsWith("/")) {
					uri += param;
				} else {
					uri += "/" + param;
				}
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
			result = DemeterWebApplication.get().getContextPath();
		}

		result += DemeterWebApplication.get().getInnerContext() + href;

		return result;
	}

	public static String getFileUri(String fileId) {
		return createUri(String.format("/dmt/getfile/%s", fileId), true);
	}

	private static IPageService getPageService() {
		if (pageService == null) {
			pageService = ModuleLoader.getApplicationContext().getBean(IPageService.class);
		}
		return pageService;
	}
}
