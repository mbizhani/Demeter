package org.devocative.demeter.service;

import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IPageService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("dmtPageService")
public class DemeterPageService implements IPageService {

	@Autowired
	private IPersistorService persistorService;

	@Override
	public void disableAllPageInfo() {
		persistorService.executeUpdate("update DPageInfo ent set ent.enabled = false");
		persistorService.commitOrRollback();
	}

	@Override
	public void addOrUpdatePageInfo(String type, String module, String uriInModule, String title) {
		DPageInfo pageInfo = persistorService
			.createQueryBuilder()
			.addFrom(DPageInfo.class, "ent")
			.addWhere("and ent.type = :type")
			.addParam("type", type)
			.object();

		if (pageInfo == null) {
			pageInfo = new DPageInfo();
			pageInfo.setType(type);
			pageInfo.setModule(module);
		}
		pageInfo.setEnabled(true);
		if (uriInModule.startsWith("/")) {
			pageInfo.setBaseUri(String.format("/%s%s", module, uriInModule));
		} else {
			pageInfo.setBaseUri(String.format("/%s/%s", module, uriInModule));
		}
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
		pageInstance.setTitle(title);
		pageInstance.setPageInfo(pageInfo);
		pageInstance.setUri(pageInfo.getBaseUri());
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

	@Override
	public DPageInstance getPageInstanceByURI(String uri) {
		return persistorService
			.createQueryBuilder()
			.addFrom(DPageInstance.class, "ent")
			.addWhere("and ent.uri = :uri")
			.addParam("uri", uri)
			.object();
	}

	public Map<String, List<DPageInstance>> getDefaultPages() {
		Map<String, List<DPageInstance>> result = new HashMap<>();
		List<DPageInstance> instances = persistorService
			.createQueryBuilder()
			.addFrom(DPageInstance.class, "ent")
			.addWhere("and ent.refId is null")
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

		return null;
	}
}
