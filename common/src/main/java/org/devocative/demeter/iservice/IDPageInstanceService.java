//overwrite
package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.filter.DPageInstanceFVO;

import java.util.List;

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
}