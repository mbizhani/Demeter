package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.DPageInstance;

import java.util.List;
import java.util.Map;

public interface IPageService {
	String D_PAGE_RESOURCE_KEY_PREFIX = "KEY:";


	void disableAllPageInfo();

	void addOrUpdatePageInfo(String type, String module, String uriInModule, String title);

	DPageInstance getPageInstanceByURI(String uri);

	Map<String, List<DPageInstance>> getDefaultPages();

	String getUriByPage(Class dPageClass);
}
