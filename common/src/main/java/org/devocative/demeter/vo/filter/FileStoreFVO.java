//overwrite
package org.devocative.demeter.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.entity.EFileStatus;
import org.devocative.demeter.entity.EFileStorage;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class FileStoreFVO implements Serializable {
	private static final long serialVersionUID = -1852367703L;

	private String name;
	private List<EFileStatus> status;
	private List<EFileStorage> storage;
	private List<EMimeType> mimeType;
	private String fileId;
	private String tag;
	private RangeVO<Date> expiration;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;
	private RangeVO<Date> modificationDate;
	private List<User> modifierUser;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<EFileStatus> getStatus() {
		return status;
	}

	public void setStatus(List<EFileStatus> status) {
		this.status = status;
	}

	public List<EFileStorage> getStorage() {
		return storage;
	}

	public void setStorage(List<EFileStorage> storage) {
		this.storage = storage;
	}

	public List<EMimeType> getMimeType() {
		return mimeType;
	}

	public void setMimeType(List<EMimeType> mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public RangeVO<Date> getExpiration() {
		return expiration;
	}

	public void setExpiration(RangeVO<Date> expiration) {
		this.expiration = expiration;
	}

	public RangeVO<Date> getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(RangeVO<Date> creationDate) {
		this.creationDate = creationDate;
	}

	public List<User> getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(List<User> creatorUser) {
		this.creatorUser = creatorUser;
	}

	public RangeVO<Date> getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(RangeVO<Date> modificationDate) {
		this.modificationDate = modificationDate;
	}

	public List<User> getModifierUser() {
		return modifierUser;
	}

	public void setModifierUser(List<User> modifierUser) {
		this.modifierUser = modifierUser;
	}

}