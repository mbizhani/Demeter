package org.devocative.demeter.web;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IPageService;
import org.devocative.wickomp.html.menu.OMenuItem;
import org.devocative.wickomp.html.menu.WMenuBar;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Index extends WebPage {
	private static final HeaderItem INDEX_CSS = CssHeaderItem.forReference(new CssResourceReference(Index.class, "wrcs/index.css"));
	private static final HeaderItem INDEX_JS = JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Index.class, "wrcs/index.js"));

	@Inject
	private IPageService pageService;

	public Index(PageParameters pageParameters) {
		TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html");
		html.add(new AttributeModifier("dir", DemeterWebSession.get().getLayoutDirection().toString()));
		add(html);

		Component content;
		DPageInstance pageInstance;
		String title = "";

		if (pageParameters.getIndexedCount() > 0) {
			StringBuilder uriBuilder = new StringBuilder();
			for (int i = 0; i < pageParameters.getIndexedCount() && i < 2; i++) {
				uriBuilder.append("/").append(pageParameters.get(i));
			}
			pageInstance = pageService.getPageInstanceByURI(uriBuilder.toString());
			if (pageInstance != null) {
				title = pageInstance.getTitle();

				List<String> params = new ArrayList<>();
				for (int i = 2; i < pageParameters.getIndexedCount(); i++) {
					params.add(pageParameters.get(i).toString());
				}
				content = createDPageFromType(pageInstance.getPageInfo().getType(), params);
			} else {
				content = new Label("content", "URI not found!");
			}
		} else {
			content = new Label("content", "");
		}

		html.add(new Label("headerTitle", title));
		html.add(content);

		TransparentWebMarkupContainer header = new TransparentWebMarkupContainer("header");
		header.add(new WMenuBar("menu", createDefaultMenus()));
		header.setVisible(pageParameters.get("printable").isNull());
		html.add(header);
	}

	private DPage createDPageFromType(String type, List<String> params) {
		try {
			Class<?> dPageClass = Class.forName(type);
			Constructor<?> constructor = dPageClass.getDeclaredConstructor(String.class, List.class);
			return (DPage) constructor.newInstance("content", params);
		} catch (Exception e) {
			throw new DSystemException("Can't create DPage: " + type, e);
		}
	}

	private List<OMenuItem> createDefaultMenus() {
		String ctx = getRequest().getContextPath() + DemeterWebApplication.get().getInnerContext();

		List<OMenuItem> result = new ArrayList<>();
		result.add(new OMenuItem(ctx, new ResourceModel("label.home")));
		Map<String, List<DPageInstance>> defaultPages = pageService.getDefaultPages();
		for (Map.Entry<String, List<DPageInstance>> entry : defaultPages.entrySet()) {
			OMenuItem moduleEntry = new OMenuItem(new Model<>(entry.getKey()));
			List<OMenuItem> subMenus = new ArrayList<>();
			for (DPageInstance pageInstance : entry.getValue()) {
				subMenus.add(new OMenuItem(ctx + pageInstance.getUri(), new Model<>(pageInstance.getTitle())));
			}
			moduleEntry.setSubMenus(subMenus);
			result.add(moduleEntry);
		}
		return result;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(INDEX_CSS);
		response.render(INDEX_JS);

		// TODO theme-based CSS loading based on user profile
		String ctx = getRequest().getContextPath();
		for (String css : DemeterWebApplication.get().getModulesRelatedCSS()) {
			response.render(CssHeaderItem.forUrl(ctx + css));
		}
	}
}
