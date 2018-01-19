package org.devocative.demeter.web;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.util.string.StringValue;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.springframework.context.ApplicationContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

public class UrlUtil {
	private static IDPageInstanceService pageInstanceService;

	// ------------------------------

	public static void redirectTo(Class<? extends DPage> dPageClass, List<String> restParams, IRequestParameters requestParameters) {
		String[] restParamsArr = null;
		if (restParams != null && !restParams.isEmpty()) {
			restParamsArr = restParams.toArray(new String[restParams.size()]);
		}
		redirectTo(dPageClass, requestParameters, restParamsArr);
	}

	public static void redirectTo(Class<? extends DPage> dPageClass, String... restParams) {
		redirectTo(dPageClass, null, restParams);
	}

	// Main Method
	public static void redirectTo(Class<? extends DPage> dPageClass, IRequestParameters requestParameters, String... restParams) {
		StringBuilder uri = new StringBuilder(createUri(dPageClass, false));

		if (restParams != null) {
			for (String param : restParams) {
				if (param.startsWith("/")) {
					uri.append(param);
				} else {
					uri.append("/").append(param);
				}
			}
		}

		if (requestParameters != null && !requestParameters.getParameterNames().isEmpty()) {
			uri.append("?");
			Set<String> parameterNames = requestParameters.getParameterNames();
			for (String parameterName : parameterNames) {
				List<StringValue> parameterValues = requestParameters.getParameterValues(parameterName);
				parameterValues.stream()
					.filter(parameterValue -> !parameterValue.isEmpty())
					.forEach(parameterValue -> {
							try {
								uri.append("&")
									.append(URLEncoder.encode(parameterName, "UTF-8"))
									.append("=")
									.append(URLEncoder.encode(parameterValue.toString(), "UTF-8"));
							} catch (UnsupportedEncodingException e) {
								throw new RuntimeException(e);
							}
						}
					);
			}
		}

		RequestCycle.get().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(uri.toString()));
	}

	// ---------------

	public static String createUri(Class<? extends DPage> dPageClass, boolean needAbsolute) {
		String href = getPageInstanceService().getUriByPage(dPageClass);

		return createUri(href, needAbsolute);
	}

	public static String createUri(DPageInstance pageInstance, boolean needAbsolute) {
		return createUri(pageInstance.getUri(), needAbsolute);
	}

	public static String createUri(String href, boolean needAbsolute) {
		String result = "";

		if (needAbsolute) {
			result = WebApplication.get().getServletContext().getContextPath();
		}

		result += DemeterWebParam.APP_INNER_CTX + href;

		return result;
	}

	// ---------------

	public static String getFileUri(String fileId) {
		return createUri(String.format("/dmt/getfile/%s", fileId), true);
	}

	// ------------------------------

	private static IDPageInstanceService getPageInstanceService() {
		if (pageInstanceService == null) {
			ApplicationContext appCtx = (ApplicationContext) WebApplication.get().getServletContext().getAttribute(DemeterWebParam.DEMETER_APP_CTX);
			pageInstanceService = appCtx.getBean(IDPageInstanceService.class);
		}
		return pageInstanceService;
	}
}
