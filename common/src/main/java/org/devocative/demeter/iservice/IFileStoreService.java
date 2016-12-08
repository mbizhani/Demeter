package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.filter.FileStoreFVO;

import java.util.Date;
import java.util.List;

public interface IFileStoreService {
	void saveOrUpdate(FileStore entity);

	FileStore load(Long id);

	List<FileStore> list();

	List<FileStore> search(FileStoreFVO filter, long pageIndex, long pageSize);

	long count(FileStoreFVO filter);

	FileStore loadByFileId(String fileId);

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	FileStoreHandler create(String name, EMimeType mimeType, Date expiration, String tag);
}