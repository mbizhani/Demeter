package org.devocative.demeter.service;

import org.devocative.demeter.DSystemException;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.core.xml.XDPage;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.ApplicationLifecyclePriority;
import org.devocative.demeter.iservice.IApplicationLifecycle;
import org.devocative.demeter.iservice.IPageService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("dmtPageService")
public class PageService implements IPageService, IApplicationLifecycle {

	@Autowired
	private IPersistorService persistorService;

	// ----------------- IApplicationLifecycle methods
	@Override
	public void init() {
		persistorService.executeUpdate("update DPageInfo ent set ent.enabled = false");

		Map<String, XModule> modules = ModuleLoader.getModules();
		for (Map.Entry<String, XModule> moduleEntry : modules.entrySet()) {
			XModule xModule = moduleEntry.getValue();

			List<XDPage> dPages = xModule.getDPages();
			for (XDPage dPage : dPages) {
				addOrUpdatePageInfo(xModule.getShortName().toLowerCase(), dPage);
			}
		}
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Low;
	}


	// ----------------- IPageService methods

	@Override
	public DPageInstance getPageInstanceByURI(String uri, String refIdParam) {
		String uri2 = uri + "/" + refIdParam;

		List<DPageInstance> instances = persistorService
			.createQueryBuilder()
			.addFrom(DPageInstance.class, "ent")
			.addWhere("and (ent.uri = :uri or ent.uri = :uri2)")
			.addParam("uri", uri)
			.addParam("uri2", uri2)
			.list();

		if (instances.size() == 1) {
			return instances.get(0);
		} else if (instances.size() == 2) {
			for (DPageInstance instance : instances) {
				if (instance.getUri().equals(uri2)) {
					return instance;
				}
			}
		}

		return null;
	}

	@Override
	public Map<String, List<DPageInstance>> getDefaultPages() {
		Map<String, List<DPageInstance>> result = new HashMap<>();

		List<DPageInstance> instances = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(DPageInstance.class, "ent")
			.addJoin("dp", "ent.pageInfo")
			.addWhere("and dp.enabled = true")
			.addWhere("and ent.inMenu = true")
			.list();

		for (DPageInstance pageInstance : instances) {
			String module = pageInstance.getPageInfo().getModule();
			if (!result.containsKey(module)) {
				result.put(module, new ArrayList<DPageInstance>());
			}
			result.get(module).add(pageInstance);
		}

		return result;
	}

	@Override
	public String getUriByPage(Class dPageClass) {
		DPageInfo pageInfo = persistorService
			.createQueryBuilder()
			.addFrom(DPageInfo.class, "ent")
			.addWhere("and ent.type = :type")
			.addParam("type", dPageClass.getName())
			.object();
		if (pageInfo != null) {
			return pageInfo.getBaseUri();
		}

		return "";
	}

	private void addOrUpdatePageInfo(String module, XDPage xdPage) {
		String baseUri;
		if (xdPage.getUri().startsWith("/")) {
			baseUri = String.format("/%s%s", module, xdPage.getUri());
		} else {
			baseUri = String.format("/%s/%s", module, xdPage.getUri());
		}

		DPageInfo pageInfo = persistorService
			.createQueryBuilder()
			.addFrom(DPageInfo.class, "ent")
			.addWhere("and (ent.type = :type or ent.baseUri = :uri)")
			.addParam("type", xdPage.getType())
			.addParam("uri", baseUri)
			.object();

		if (pageInfo == null) {
			pageInfo = new DPageInfo();
		}
		pageInfo.setType(xdPage.getType());
		pageInfo.setModule(module);
		pageInfo.setEnabled(true);
		pageInfo.setBaseUri(baseUri);
		persistorService.saveOrUpdate(pageInfo);

		DPageInstance pageInstance = persistorService
			.createQueryBuilder()
			.addFrom(DPageInstance.class, "ent")
			.addWhere("and ent.pageInfo.id = :pageId")
			.addWhere("and ent.refId is null")
			.addParam("pageId", pageInfo.getId())
			.object();

		if (pageInstance == null) {
			pageInstance = new DPageInstance();
		}
		if (xdPage.getTitle() != null && xdPage.getTitle().startsWith(D_PAGE_RESOURCE_KEY_PREFIX)) {
			String prefix = String.format("%sdPage.%s.", D_PAGE_RESOURCE_KEY_PREFIX, module);
			if (!xdPage.getTitle().startsWith(prefix)) {
				throw new DSystemException("Invalid DPage title key: " + xdPage.getTitle());
			}
		}
		pageInstance.setTitle(xdPage.getTitle());
		pageInstance.setInMenu(xdPage.getInMenu());
		pageInstance.setPageInfo(pageInfo);
		pageInstance.setUri(pageInfo.getBaseUri()); //Duplicated for performance issue
		persistorService.saveOrUpdate(pageInstance);

		persistorService
			.createQueryBuilder()
			.addSelect("update DPageInstance ent set ent.uri = concat('/',:base_uri, '/', ent.refId)")
			.addWhere("and ent.pageInfo.id = :id")
			.addWhere("and ent.refId is not null")
			.addParam("base_uri", pageInfo.getBaseUri())
			.addParam("id", pageInfo.getId())
			.update();

		persistorService.commitOrRollback();
	}
}
