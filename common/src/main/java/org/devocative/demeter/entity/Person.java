package org.devocative.demeter.entity;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;

@Audited
@Entity
@Table(name = "t_dmt_person")
@Inheritance(strategy = InheritanceType.JOINED)
public class Person implements IRowMode, ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	private static final long serialVersionUID = 6377393773056642925L;

	@Id
	@GeneratedValue(generator = "dmt_person")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_person", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_person")
		})
	private Long id;

	//@NotNull
	@Column(name = "c_first_name")
	private String firstName;

	//@NotNull
	@Column(name = "c_last_name")
	private String lastName;

	//@Past
	@Column(name = "d_birth_reg", columnDefinition = "date")
	private Date birthRegDate;

	//@Email
	@Column(name = "c_email")
	private String email;

	//@Pattern(regexp = "0\\d{10}", message = "MobileValidation")
	@Column(name = "c_mobile")
	private String mobile;

	@Column(name = "c_sys_number")
	private String systemNumber;

	@Column(name = "b_has_user", nullable = false)
	private Boolean hasUser = false;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "person")
	private User user;

	//----------------------------- CREATE / MODIFY

	@Embedded
	@AttributeOverride(name = "id", column = @Column(name = "e_mod", nullable = false))
	private ERowMode rowMode;

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "prsn_crtrusr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user")
	private Long creatorUserId;

	@NotAudited
	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "prsn_mdfrusr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	// ---------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getBirthRegDate() {
		return birthRegDate;
	}

	public void setBirthRegDate(Date birthRegDate) {
		this.birthRegDate = birthRegDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSystemNumber() {
		return systemNumber;
	}

	public void setSystemNumber(String systemNumber) {
		this.systemNumber = systemNumber;
	}

	public Boolean getHasUser() {
		return hasUser;
	}

	public void setHasUser(Boolean hasUser) {
		this.hasUser = hasUser;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
		if (!(o instanceof Person)) return false;

		Person person = (Person) o;

		if (getId() != null ? !getId().equals(person.getId()) : person.getId() != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return String.format("%s/%s", getFirstName(), getLastName());
	}
}
