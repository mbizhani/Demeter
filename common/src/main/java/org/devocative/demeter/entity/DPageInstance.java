package org.devocative.demeter.entity;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_dmt_d_page_inst", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_pageinst_uri", columnNames = {"c_uri"})
})
public class DPageInstance implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = -6082928981155342914L;

	@Id
	@GeneratedValue(generator = "dmt_d_page")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_d_page", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_d_page")
		})
	private Long id;

	@Column(name = "c_title", nullable = false)
	private String title;

	@Column(name = "c_uri", nullable = false)
	private String uri;

	@Column(name = "b_in_menu", nullable = false)
	private Boolean inMenu;

	@NotAudited
	@Column(name = "c_ref_id")
	private String refId;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_page_info", nullable = false, foreignKey = @ForeignKey(name = "pageinst2pageinfo"))
	private DPageInfo pageInfo;

	@OrderBy("name")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_pageinst_role",
		joinColumns = {@JoinColumn(name = "f_page_inst", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		foreignKey = @ForeignKey(name = "pageInstRole2pageInst"),
		inverseForeignKey = @ForeignKey(name = "pageInstRole2role"),
		uniqueConstraints = {@UniqueConstraint(name = "uk_dmt_mtPageInstRole", columnNames = {"f_page_inst", "f_role"})}
	)
	private List<Role> roles;

	// ---------------

	@Transient
	private String icon;

	// ---------------

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "pageinst_crtrusr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@NotAudited
	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "pageinst_mdfrusr2user"))
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Boolean getInMenu() {
		return inMenu;
	}

	public void setInMenu(Boolean inMenu) {
		this.inMenu = inMenu;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public DPageInfo getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(DPageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	// ---------------

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	// ---------------

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
		if (!(o instanceof DPageInstance)) return false;

		DPageInstance that = (DPageInstance) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getUri();
	}
}
