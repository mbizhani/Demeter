//overwrite
package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.filter.DPageInstanceFVO;

import java.util.List;
import java.util.Map;

public interface IDPageInstanceService {
	void saveOrUpdate(DPageInstance entity);

	DPageInstance load(Long id);

	DPageInstance loadByUri(String uri);

	List<DPageInstance> list();

	List<DPageInstance> search(DPageInstanceFVO filter, long pageIndex, long pageSize);

	long count(DPageInstanceFVO filter);

	List<DPageInfo> getPageInfoList();

	List<Role> getRolesList();

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	String D_PAGE_RESOURCE_KEY_PREFIX = "KEY:";

	DPageInstance getPageInstanceByURI(String uri, String refIdParam);

	Map<String, List<DPageInstance>> getDefaultPages();

	String getUriByPage(Class dPageClass);
}