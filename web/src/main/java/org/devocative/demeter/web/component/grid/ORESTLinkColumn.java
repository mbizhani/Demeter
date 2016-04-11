package org.devocative.demeter.web.component.grid;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.model.IModel;
import org.devocative.demeter.web.DPage;
import org.devocative.demeter.web.UrlUtil;
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
		String baseUri = UrlUtil.createUri(dPageClass, true);
		Object firstParam = PropertyResolver.getValue(firstParamProperty, bean);
		return String.format("<a href=\"%s/%s\">%s</a>", baseUri, firstParam, link.toString());
	}

	@Override
	public String footerCellValue(Object o, int i, String s) {
		throw new RuntimeException("Footer not supported in ORESTLinkColumn");
	}
}
