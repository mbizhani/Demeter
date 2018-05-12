package org.devocative.demeter.service;

import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.FileStoreHandler;
import org.devocative.demeter.iservice.IFileStoreService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.vo.filter.FileStoreFVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service("dmtFileStoreService")
public class FileStoreService implements IFileStoreService {
	private static final Logger logger = LoggerFactory.getLogger(FileStoreService.class);

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private ISecurityService securityService;

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
	public FileStore loadByFileId(String fileId) {
		FileStore result = persistorService
			.createQueryBuilder()
			.addFrom(FileStore.class, "ent")
			.addWhere("and ent.fileId = :fileId", "fileId", fileId)
			.object();

		if (result.getStatus() == EFileStatus.VALID) {
			if (result.getStorage() == EFileStorage.DISK && !new File(result.getPath()).exists()) {
				result.setStatus(EFileStatus.DELETED);
				saveOrUpdate(result);
			}
		}

		return result;
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
	public List<User> getCreatorUserList() {
		return persistorService.list(User.class);
	}

	@Override
	public List<User> getModifierUserList() {
		return persistorService.list(User.class);
	}

	// ==============================

	@Override
	public void saveOrUpdate(FileStore entity, byte[] bytes) {
		String oldFileId = entity.getFileId();

		OutputStream outputStream = createOutputStream(entity);
		try {
			outputStream.write(bytes);
			outputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		saveOrUpdate(entity);

		if (oldFileId != null) {
			moveFileAsExpired(oldFileId, EFileStorage.DISK); //TODO
		}
	}

	@Override
	public FileStoreHandler create(String name, EFileStorage storage, EMimeType mimeType, Date expiration, String... tags) {
		FileStore fileStore = new FileStore();
		fileStore.setName(name);
		fileStore.setStorage(storage);
		fileStore.setStatus(EFileStatus.VALID);
		fileStore.setMimeType(mimeType);
		fileStore.setExpiration(expiration);
		if (tags != null && tags.length > 0) {
			String tag = tags[0];
			for (int i = 1; i < tags.length; i++) {
				tag += "," + tags[i];
			}
			fileStore.setTag(tag);
		}

		OutputStream outputStream = createOutputStream(fileStore);
		return new FileStoreHandler(this, outputStream, fileStore);
	}

	@Override
	public void writeFile(FileStore fileStore, OutputStream outputStream) {
		if (EFileStorage.DISK.equals(fileStore.getStorage())) {
			String path = fileStore.getPath();

			try (FileInputStream inputStream = new FileInputStream(path)) {
				IOUtils.copy(inputStream, outputStream);
			} catch (IOException e) {
				throw new DSystemException("Can't open file: " + path, e);
			}
		} else if (EFileStorage.DATA_BASE.equals(fileStore.getStorage())) {
			throw new RuntimeException("Database as storage is not implemented!");
		} else {
			throw new RuntimeException("Invalid storage for FileStore: " + fileStore.getStorage());
		}
	}

	@Override
	public void doExpire() {
		logger.info("Starting FileStoreDTask ...");

		try {
			File expiredDir = getExpiredDir();
			logger.info("Deleting Old Expired Files");
			FileUtils.deleteDirectory(expiredDir);

			Date now = new Date();
			List<String> fileIds = listOfExpiredFiles(now);
			logger.info("Start Expiring Files: no=[{}]", fileIds.size());

			if (!fileIds.isEmpty()) {
				for (String id : fileIds) {
					moveFileAsExpired(id, EFileStorage.DISK); //TODO
				}

				logger.info("Files Moved to Expired Dir");

				int updated = updateExpiredFilesStatus(now);

				logger.info("Expired files status are updated: no=[{}]", updated);
			}

		} catch (IOException e) {
			logger.error("FileStoreDTask: ", e);
		}

	}

	@Override
	public List<FileStore> listByCurrentUserAsCreator() {
		UserVO currentUser = securityService.getCurrentUser();
		return persistorService.createQueryBuilder()
			.addFrom(FileStore.class, "ent")
			.addWhere("and ent.creatorUserId = :userId")
			.addParam("userId", currentUser.getUserId())
			.list();
	}

	// ------------------------------

	private OutputStream createOutputStream(FileStore fileStore) {
		if (EFileStorage.DISK.equals(fileStore.getStorage())) {
			String fileId = UUID.randomUUID().toString().replaceAll("-", "");
			fileStore.setFileId(fileId);

			File baseDir = new File(ConfigUtil.getString(DemeterConfigKey.FileBaseDir));

			if (!baseDir.exists()) {
				baseDir.mkdirs();
			} else if (!baseDir.isDirectory()) {
				throw new DSystemException("Invalid base directory for file: " + ConfigUtil.getString(DemeterConfigKey.FileBaseDir));
			}

			String fileFQN = baseDir.getAbsolutePath() + File.separator + fileId;

			try {
				return new FileOutputStream(fileFQN);
			} catch (FileNotFoundException e) {
				throw new DSystemException("Can't create file: " + fileFQN, e);
			}
		} else if (EFileStorage.DATA_BASE.equals(fileStore.getStorage())) {
			throw new RuntimeException("Database as storage is not implemented!");
		} else {
			throw new RuntimeException("Invalid storage for FileStore: " + fileStore.getStorage());
		}
	}

	private List<String> listOfExpiredFiles(Date dt) {
		return persistorService
			.createQueryBuilder()
			.addSelect("select ent.fileId")
			.addFrom(FileStore.class, "ent")
			.addWhere("and ent.expiration < :dt")
			.addParam("dt", dt)
			.addWhere("and ent.status = :st")
			.addParam("st", EFileStatus.VALID)
			.list();
	}

	private int updateExpiredFilesStatus(Date dt) {
		return persistorService
			.createQueryBuilder()
			.addSelect("update FileStore ent set ent.status = :new_st")
			.addParam("new_st", EFileStatus.EXPIRED)
			.addWhere("and ent.expiration < :dt")
			.addParam("dt", dt)
			.addWhere("and ent.status = :old_st")
			.addParam("old_st", EFileStatus.VALID)
			.update();
	}

	private void moveFileAsExpired(String id, EFileStorage storage) {
		if (EFileStorage.DISK.equals(storage)) {
			File expiredDir = getExpiredDir();
			String f = ConfigUtil.getString(DemeterConfigKey.FileBaseDir) + File.separator + id;
			File file = new File(f);
			if (file.exists()) {
				try {
					FileUtils.moveFileToDirectory(file, expiredDir, false);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			throw new RuntimeException("File Storage Not Supported: " + storage);
		}
	}

	private File getExpiredDir() {
		String dir = ConfigUtil.getString(DemeterConfigKey.FileBaseDir) + File.separator + "EXPIRED";
		File expiredDir = new File(dir);

		if (!expiredDir.exists()) {
			expiredDir.mkdirs();
		} else if (!expiredDir.isDirectory()) {
			throw new RuntimeException("Invalid directory for expired dir: " + expiredDir);
		}

		return expiredDir;
	}
}