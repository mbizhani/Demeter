//overwrite
package org.devocative.demeter.service;

import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.core.xml.XDPage;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.*;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.vo.filter.DPageInstanceFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("dmtDPageInstanceService")
public class DPageInstanceService implements IDPageInstanceService, IApplicationLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(DPageInstanceService.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(DPageInstance entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public DPageInstance load(Long id) {
		return persistorService.get(DPageInstance.class, id);
	}

	@Override
	public DPageInstance loadByUri(String uri) {
		return persistorService
			.createQueryBuilder()
			.addFrom(DPageInstance.class, "ent")
			.addWhere("and ent.uri = :uri")
			.addParam("uri", uri)
			.object();
	}

	@Override
	public List<DPageInstance> list() {
		return persistorService.list(DPageInstance.class);
	}

	@Override
	public List<DPageInstance> search(DPageInstanceFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(DPageInstance.class, "ent")
			.applyFilter(DPageInstance.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(DPageInstanceFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(DPageInstance.class, "ent")
			.applyFilter(DPageInstance.class, "ent", filter)
			.object();
	}

	@Override
	public List<DPageInfo> getPageInfoList() {
		return persistorService.list(DPageInfo.class);
	}

	@Override
	public List<Role> getRolesList() {
		return persistorService.list(Role.class);
	}

	@Override
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getModifierUserList() {
		return persistorService.list(User.class);
	}

	// ==============================

	private ICache<String, DPageInstance> pageInstCache;
	private ICache<Class, String> uriCache;

	@Autowired
	private ICacheService cacheService;

	@Autowired
	private IRoleService roleService;

	// ------------------------------ IApplicationLifecycle methods

	@Override
	public void init() {
		persistorService.executeUpdate("update DPageInfo ent set ent.enabled = false");

		int totalDPageSize = 0;
		Map<String, XModule> modules = ModuleLoader.getModules();
		for (Map.Entry<String, XModule> moduleEntry : modules.entrySet()) {
			XModule xModule = moduleEntry.getValue();

			List<XDPage> dPages = xModule.getDPages();
			if (dPages != null) {
				for (XDPage dPage : dPages) {
					addOrUpdatePageInfo(xModule.getShortName().toLowerCase(), dPage);
				}
				totalDPageSize += dPages.size();
			}
		}
		persistorService.commitOrRollback();

		pageInstCache = cacheService.create("DMT_D_PAGE_INST", totalDPageSize * 2);
		pageInstCache.setMissedHitHandler(new IMissedHitHandler<String, DPageInstance>() {
			@Override
			public DPageInstance loadForCache(String key) {
				return persistorService
					.createQueryBuilder()
					.addFrom(DPageInstance.class, "ent")
					.addWhere("and ent.uri = :uri")
					.addParam("uri", key)
					.object();
			}
		});

		uriCache = cacheService.create("DMT_D_PAGE_URI", totalDPageSize * 2);
		uriCache.setMissedHitHandler(new IMissedHitHandler<Class, String>() {
			@Override
			public String loadForCache(Class key) {
				DPageInfo pageInfo = persistorService
					.createQueryBuilder()
					.addFrom(DPageInfo.class, "ent")
					.addWhere("and (ent.type = :type or ent.typeAlt = :type)")
					.addParam("type", key.getName())
					.object();

				if (pageInfo != null) {
					return pageInfo.getBaseUri();
				}

				return "";
			}
		});
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Low;
	}

	// ------------------------------ IPageService methods

	@Override
	public DPageInstance getPageInstanceByURI(String uri, String refIdParam) {
		if (refIdParam != null) {
			String uri2 = uri + "/" + refIdParam;
			if (pageInstCache.containsKeyOrFetch(uri2)) {
				return pageInstCache.get(uri2);
			}
		}

		return pageInstCache.get(uri);
	}

	@Override
	public UserVO.PageVO getDefaultPages() {
		List<DPageInstance> instances = persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(DPageInstance.class, "ent")
			.addJoin("dp", "ent.pageInfo", EJoinMode.LeftFetch)
			.addWhere("and dp.enabled = true")
			.addWhere("and ent.inMenu = true")
			.list();

		Set<String> accessibleUri = new HashSet<>();
		Map<String, List<DPageInstance>> menuEntries = new HashMap<>();

		for (DPageInstance pageInstance : instances) {
			DPageInfo pageInfo = pageInstance.getPageInfo();

			String module = pageInfo.getModule();
			if (!menuEntries.containsKey(module)) {
				menuEntries.put(module, new ArrayList<DPageInstance>());
			}
			menuEntries.get(module).add(pageInstance);

			accessibleUri.add(pageInstance.getUri());
		}

		return new UserVO.PageVO(accessibleUri, menuEntries);
	}

	@Override
	public String getUriByPage(Class dPageClass) {
		return uriCache.get(dPageClass);
	}

	@Override
	public UserVO.PageVO getAccessiblePages(Collection<Role> roles) {
		IQueryBuilder queryBuilder = persistorService.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(DPageInstance.class, "ent")
			.addJoin("pi", "ent.pageInfo", EJoinMode.LeftFetch)
			.addJoin("rl", "ent.roles", EJoinMode.Left)
			.addWhere("and pi.enabled = true");

		if (roles != null && !roles.isEmpty()) {
			queryBuilder
				.addWhere("and (rl in (:roles) or rl.id is null)")
				.addParam("roles", roles);
		} else {
			queryBuilder.addWhere("and rl.id is null");
		}

		List<DPageInstance> instances = queryBuilder.list();

		Set<String> accessibleUri = new HashSet<>();
		Map<String, List<DPageInstance>> menuEntries = new HashMap<>();

		for (DPageInstance pageInstance : instances) {
			DPageInfo pageInfo = pageInstance.getPageInfo();

			String module = pageInfo.getModule();
			if (pageInstance.getInMenu()) {
				if (!menuEntries.containsKey(module)) {
					menuEntries.put(module, new ArrayList<DPageInstance>());
				}
				menuEntries.get(module).add(pageInstance);
			}

			accessibleUri.add(pageInstance.getUri());
		}

		return new UserVO.PageVO(accessibleUri, menuEntries);
	}

	// ------------------------------

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
			.addWhere("and (ent.type = :type or ent.typeAlt = :type or ent.baseUri = :uri)")
			.addParam("type", xdPage.getType())
			.addParam("uri", baseUri)
			.object();

		if (pageInfo == null) {
			pageInfo = new DPageInfo();
			pageInfo.setType(xdPage.getType());
		} else if (!pageInfo.getType().equals(xdPage.getType())) {
			pageInfo.setTypeAlt(xdPage.getType());
		}
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
		if (xdPage.getRoles() != null && !xdPage.getRoles().isEmpty()) {
			List<Role> roles = new ArrayList<>();
			String[] roleNames = xdPage.getRoles().split("[,]");
			for (String roleName : roleNames) {
				Role role = roleService.loadByName(roleName);
				if (role != null) {
					roles.add(role);
				} else {
					throw new RuntimeException(String.format("Invalid role name [%s] for DPage [%s]", roleName, xdPage.getUri()));
				}
			}
			pageInstance.setRoles(roles);
		}
		persistorService.saveOrUpdate(pageInstance);
	}
}