package org.devocative.demeter.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Audited
@Entity
@Table(name = "t_dmt_user", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_user_username", columnNames = {"c_username"})
})
public class User implements Serializable {
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
	private EAuthMechanism authMechanism;

	//@NotNull
	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_status", nullable = false))
	private EUserStatus status = EUserStatus.ENABLED;

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_locale", nullable = false))
	private ELocale locale = ELocale.FA;

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

	@NotAudited
	@Column(name = "c_constraints", length = 1000)
	private String constraints;

	@Column(name = "b_admin", nullable = false)
	private Boolean admin = false;

	@Column(name = "n_session_timeout", nullable = false)
	private Integer sessionTimeout = 60;

	@OneToOne(fetch = FetchType.EAGER)
	@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "user2person"))
	private Person person;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "mt_dmt_user_role",
		joinColumns = {@JoinColumn(name = "f_user", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "f_role", nullable = false)},
		foreignKey = @ForeignKey(name = "userrole2user"),
		inverseForeignKey = @ForeignKey(name = "userrole2role")
	)
	private List<Role> roles;

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

	public String getConstraints() {
		return constraints;
	}

	public void setConstraints(String constraints) {
		this.constraints = constraints;
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

	// ------------------------------

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
}
