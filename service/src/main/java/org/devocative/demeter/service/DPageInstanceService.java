//overwrite
package org.devocative.demeter.service;

import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.entity.Role;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.filter.DPageInstanceFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dmtDPageInstanceService")
public class DPageInstanceService implements IDPageInstanceService {
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
}