package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.entity.EFileStatus;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.FileStoreHandler;
import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.filter.FileStoreFVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service("dmtFileStoreService")
public class FileStoreService implements IFileStoreService {

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	@Override
	public void saveOrUpdate(FileStore entity) {
		persistorService.saveOrUpdate(entity);
	}

	@Override
	public FileStore load(Long id) {
		return persistorService.get(FileStore.class, id);
	}

	@Override
	public List<FileStore> list() {
		return persistorService.list(FileStore.class);
	}

	@Override
	public List<FileStore> search(FileStoreFVO filter, long pageIndex, long pageSize) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent")
			.addFrom(FileStore.class, "ent")
			.applyFilter(FileStore.class, "ent", filter)
			.list((pageIndex - 1) * pageSize, pageSize);
	}

	@Override
	public long count(FileStoreFVO filter) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(FileStore.class, "ent")
			.applyFilter(FileStore.class, "ent", filter)
			.object();
	}

	@Override
	public FileStore loadByFileId(String fileId) {
		return persistorService
			.createQueryBuilder()
			.addFrom(FileStore.class, "ent")
			.addWhere("and ent.fileId = :fileId")
			.addParam("fileId", fileId)
			.object();
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

	@Override
	public FileStoreHandler create(String name, EMimeType mimeType, Date expiration, String tag) {
		File baseDir = new File(ConfigUtil.getString(DemeterConfigKey.FileBaseDir));

		if (!baseDir.exists()) {
			baseDir.mkdirs();
		} else if (!baseDir.isDirectory()) {
			throw new DSystemException("Invalid base directory for file: " + ConfigUtil.getString(DemeterConfigKey.FileBaseDir));
		}

		String fileId = UUID.randomUUID().toString().replaceAll("-", "");
		String fileFQN = baseDir.getAbsolutePath() + File.separator + fileId;

		FileStore fileStore = new FileStore();
		fileStore.setName(name);
		fileStore.setStatus(EFileStatus.VALID);
		fileStore.setMimeType(mimeType);
		fileStore.setFileId(fileId);
		fileStore.setExpiration(expiration);
		fileStore.setTag(tag);

		try {
			return new FileStoreHandler(this, new FileOutputStream(fileFQN), fileStore);
		} catch (FileNotFoundException e) {
			throw new DSystemException("Can't create file: " + fileFQN, e);
		}
	}

}