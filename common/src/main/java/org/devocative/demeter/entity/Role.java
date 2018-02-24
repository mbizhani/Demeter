package org.devocative.demeter.entity;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_dmt_role", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_role_name", columnNames = {"c_name"})
})
public class Role implements IRowMode, ICreationDate, ICreatorUser, IModificationDate, IModifierUser, Comparable<Role> {
	private static final long serialVersionUID = -7388401924357240473L;

	@Id
	@GeneratedValue(generator = "dmt_role")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_role", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_role")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_role_mode", nullable = false))
	private ERoleMode roleMode = ERoleMode.NORMAL;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_prvlg_role_perm",
		joinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_prvlg", nullable = false)},
		foreignKey = @ForeignKey(name = "prvlgRolePerm2role"),
		inverseForeignKey = @ForeignKey(name = "prvlgRolePerm2prvlg"),
		uniqueConstraints = {@UniqueConstraint(name = "uk_dmt_mtPrvlgRolePerm", columnNames = {"f_role", "f_prvlg"})}
	)
	private List<Privilege> permissions;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_prvlg_role_deny",
		joinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_prvlg", nullable = false)},
		foreignKey = @ForeignKey(name = "prvlgRoleDeny2role"),
		inverseForeignKey = @ForeignKey(name = "prvlgRoleDeny2prvlg"),
		uniqueConstraints = {@UniqueConstraint(name = "uk_dmt_mtPrvlgRoleDeny", columnNames = {"f_role", "f_prvlg"})}
	)
	private List<Privilege> denials;

	// ---------------

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_mod", nullable = false))
	private ERowMode rowMode;

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "role_crtrusr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user", nullable = false)
	private Long creatorUserId;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "role_mdfrusr2user"))
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

	public ERoleMode getRoleMode() {
		return roleMode;
	}

	public Role setRoleMode(ERoleMode roleMode) {
		this.roleMode = roleMode;
		return this;
	}

	public List<Privilege> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Privilege> permissions) {
		this.permissions = permissions;
	}

	public List<Privilege> getDenials() {
		return denials;
	}

	public void setDenials(List<Privilege> denials) {
		this.denials = denials;
	}

	// ---------------

	@Override
	public ERowMode getRowMode() {
		return rowMode;
	}

	@Override
	public void setRowMode(ERowMode rowMode) {
		this.rowMode = rowMode;
	}

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
		if (!(o instanceof Role)) return false;

		Role role = (Role) o;

		return !(getId() != null ? !getId().equals(role.getId()) : role.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		String str = getName();

		if (ERoleMode.MAIN.equals(getRoleMode())) {
			str += "*";
		}
		return str;
	}

	@Override
	public int compareTo(Role o) {
		return getName().compareTo(o.getName());
	}
}
