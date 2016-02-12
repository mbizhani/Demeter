package org.devocative.demeter.web.component.grid;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.iservice.IPageService;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.DemeterWebApplication;
import org.devocative.wickomp.grid.column.OColumn;
import org.devocative.wickomp.html.HTMLBase;

public class ORESTLinkColumn<T> extends OColumn<T> {
	private Class<? extends DPage> dPageClass;
	private String firstParamProperty;
	private HTMLBase link;

	public ORESTLinkColumn(IModel<String> title, Class<? extends DPage> dPageClass, String firstParamProperty, HTMLBase link) {
		super(title);
		this.dPageClass = dPageClass;
		this.firstParamProperty = firstParamProperty;
		this.link = link;
	}

	@Override
	public String cellValue(T bean, String id, int colNo, String url) {
		IPageService pageService = ModuleLoader.getApplicationContext().getBean(IPageService.class);
		String href = pageService.getUriByPage(dPageClass);

		Object firstParam = PropertyResolver.getValue(firstParamProperty, bean);
		String ctx = RequestCycle.get().getRequest().getContextPath() +
			DemeterWebApplication.get().getInnerContext();
		return String.format("<a href=\"%s%s/%s\">%s</a>", ctx, href, firstParam, link.toString());
	}
}
