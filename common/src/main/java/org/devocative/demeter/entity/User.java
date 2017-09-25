package org.devocative.demeter.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_dmt_user", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_user_username", columnNames = {"c_username"})
})
public class User implements IRowMod, ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = 1580426811623477680L;

	@Id
	@GeneratedValue(generator = "SharedPrimaryKeyGenerator")
	@GenericGenerator(name = "SharedPrimaryKeyGenerator",
		strategy = "foreign",
		parameters = {@org.hibernate.annotations.Parameter(name = "property", value = "person")})
	private Long id;

	//@NotNull
	@Column(name = "c_username", nullable = false)
	private String username;

	@NotAudited
	@Column(name = "c_password")
	private String password;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_auth_mech", nullable = false))
	private EAuthMechanism authMechanism = EAuthMechanism.DATABASE;

	//@NotNull
	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_status", nullable = false))
	private EUserStatus status = EUserStatus.ENABLED;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_locale"))
	private ELocale locale;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_cal_type"))
	private ECalendar calendarType;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_laydir_type"))
	private ELayoutDirection layoutDirectionType;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_date_pattern"))
	private EDatePatternType datePatternType;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_date_time_pattern"))
	private EDateTimePatternType dateTimePatternType;

	@NotAudited
	@Column(name = "d_last_login", columnDefinition = "date")
	private Date lastLoginDate;

	@Column(name = "b_admin", nullable = false)
	private Boolean admin = false;

	@Column(name = "n_session_timeout")
	private Integer sessionTimeout;

	@OneToOne(fetch = FetchType.EAGER)
	@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "user2person"))
	private Person person;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_user_role",
		joinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		foreignKey = @ForeignKey(name = "userRole2user"),
		inverseForeignKey = @ForeignKey(name = "userRole2role")
	)
	private List<Role> roles;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_prvlg_user_perm",
		joinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_prvlg", nullable = false)},
		foreignKey = @ForeignKey(name = "prvlgUserPerm2user"),
		inverseForeignKey = @ForeignKey(name = "prvlgUserPerm2prvlg")
	)
	private List<Privilege> permissions;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_prvlg_user_deny",
		joinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_prvlg", nullable = false)},
		foreignKey = @ForeignKey(name = "prvlgUserDeny2user"),
		inverseForeignKey = @ForeignKey(name = "prvlgUserDeny2prvlg")
	)
	private List<Privilege> denials;

	// ------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public EAuthMechanism getAuthMechanism() {
		return authMechanism;
	}

	public void setAuthMechanism(EAuthMechanism authMechanism) {
		this.authMechanism = authMechanism;
	}

	public EUserStatus getStatus() {
		return status;
	}

	public void setStatus(EUserStatus status) {
		this.status = status;
	}

	public ELocale getLocale() {
		return locale;
	}

	public void setLocale(ELocale locale) {
		this.locale = locale;
	}

	public ECalendar getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(ECalendar calendarType) {
		this.calendarType = calendarType;
	}

	public ELayoutDirection getLayoutDirectionType() {
		return layoutDirectionType;
	}

	public void setLayoutDirectionType(ELayoutDirection layoutDirectionType) {
		this.layoutDirectionType = layoutDirectionType;
	}

	public EDatePatternType getDatePatternType() {
		return datePatternType;
	}

	public void setDatePatternType(EDatePatternType datePatternType) {
		this.datePatternType = datePatternType;
	}

	public EDateTimePatternType getDateTimePatternType() {
		return dateTimePatternType;
	}

	public void setDateTimePatternType(EDateTimePatternType dateTimePatternType) {
		this.dateTimePatternType = dateTimePatternType;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public Integer getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
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
	public ERowMod getRowMod() {
		return getPersonSafely().getRowMod();
	}

	@Override
	public void setRowMod(ERowMod rowMod) {
		getPersonSafely().setRowMod(rowMod);
	}

	@Override
	public Date getCreationDate() {
		return getPersonSafely().getCreationDate();
	}

	@Override
	public void setCreationDate(Date date) {
		getPersonSafely().setCreationDate(date);
	}

	@Override
	public Long getCreatorUserId() {
		return getPersonSafely().getCreatorUserId();
	}

	@Override
	public void setCreatorUserId(Long userId) {
		getPersonSafely().setCreatorUserId(userId);
	}

	@Override
	public Date getModificationDate() {
		return getPersonSafely().getModificationDate();
	}

	@Override
	public void setModificationDate(Date date) {
		getPersonSafely().setModificationDate(date);
	}

	@Override
	public Long getModifierUserId() {
		return getPersonSafely().getModifierUserId();
	}

	@Override
	public void setModifierUserId(Long userId) {
		getPersonSafely().setModifierUserId(userId);
	}

	@Override
	public Integer getVersion() {
		return getPersonSafely().getVersion();
	}

	@Override
	public void setVersion(Integer version) {
		getPersonSafely().setVersion(version);
	}

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;

		User user = (User) o;

		if (getId() != null ? !getId().equals(user.getId()) : user.getId() != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getUsername() != null ? getUsername() : String.format("[%s]", getId());
	}

	// ---------------

	public Person getPersonSafely() {
		if (getPerson() == null) {
			setPerson(new Person());
		}
		return getPerson();
	}
}
