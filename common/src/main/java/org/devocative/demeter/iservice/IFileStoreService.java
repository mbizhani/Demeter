package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.EFileStorage;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.vo.filter.FileStoreFVO;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public interface IFileStoreService {
	void saveOrUpdate(FileStore entity);

	void saveOrUpdate(FileStore entity, byte[] bytes);

	FileStore load(Long id);

	FileStore loadByFileId(String fileId);

	List<FileStore> list();

	List<FileStore> search(FileStoreFVO filter, long pageIndex, long pageSize);

	long count(FileStoreFVO filter);

	List<User> getCreatorUserList();

	List<User> getModifierUserList();

	// ==============================

	FileStoreHandler create(String name, EFileStorage storage, EMimeType mimeType, Date expiration, String... tags);

	void writeFile(FileStore fileStore, OutputStream outputStream);

	void doExpire();

	List<FileStore> listByCurrentUserAsCreator();
}