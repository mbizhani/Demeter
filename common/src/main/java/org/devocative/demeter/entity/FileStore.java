package org.devocative.demeter.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_dmt_file_store", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_filestore_fileid", columnNames = {"c_file_id"})
})
public class FileStore implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = -1852486715470084613L;

	@Id
	@GeneratedValue(generator = "dmt_file_store")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_file_store", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_file_store")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_status", nullable = false))
	private EFileStatus status;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_storage", nullable = false))
	private EFileStorage storage;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_mime_type", nullable = false))
	private EMimeType mimeType;

	@Column(name = "c_file_id", nullable = false)
	private String fileId;

	@Column(name = "c_tag")
	private String tag;

	@Column(name = "d_expiration", columnDefinition = "date")
	private Date expiration;

	// --------------- CREATE / MODIFY

	//@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	//@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "filestore_crtrusr2user"))
	private User creatorUser;

	//@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "filestore_mdfrusr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EFileStatus getStatus() {
		return status;
	}

	public void setStatus(EFileStatus status) {
		this.status = status;
	}

	public EFileStorage getStorage() {
		return storage;
	}

	public FileStore setStorage(EFileStorage storage) {
		this.storage = storage;
		return this;
	}

	public EMimeType getMimeType() {
		return mimeType;
	}

	public void setMimeType(EMimeType mimeType) {
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

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	// --------------- CREATE / MODIFY

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public User getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
	}

	@Override
	public Long getCreatorUserId() {
		return creatorUserId;
	}

	@Override
	public void setCreatorUserId(Long creatorUserId) {
		this.creatorUserId = creatorUserId;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public User getModifierUser() {
		return modifierUser;
	}

	public void setModifierUser(User modifierUser) {
		this.modifierUser = modifierUser;
	}

	@Override
	public Long getModifierUserId() {
		return modifierUserId;
	}

	@Override
	public void setModifierUserId(Long modifierUserId) {
		this.modifierUserId = modifierUserId;
	}

	@Override
	public Integer getVersion() {
		return version;
	}

	@Override
	public void setVersion(Integer version) {
		this.version = version;
	}

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FileStore)) return false;

		FileStore fileStore = (FileStore) o;

		return !(getId() != null ? !getId().equals(fileStore.getId()) : fileStore.getId() != null);
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : String.format("[%s]", getId());
	}
}
