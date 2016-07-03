package org.devocative.demeter.entity;

import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_dmt_d_task", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_task_type", columnNames = {"c_type"})
})
public class DTaskInfo implements ICreationDate, ICreatorUser, IModificationDate, IModifierUser {
	@Id
	@GeneratedValue(generator = "dmt_d_task")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_d_task", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_d_task")
		})
	private Long id;

	@Column(name = "c_type", nullable = false)
	private String type;

	@Column(name = "c_module", nullable = false)
	private String module;

	@Column(name = "b_enabled", nullable = false)
	private Boolean enabled = true;

	//----------------------------- CREATE / MODIFY

	@NotAudited
	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_creator_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "task_crtrusr2user"))
	private User creatorUser;

	@NotAudited
	@Column(name = "f_creator_user")
	private Long creatorUserId;

	@NotAudited
	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "f_modifier_user", insertable = false, updatable = false,
		foreignKey = @ForeignKey(name = "task_mdfrusr2user"))
	private User modifierUser;

	@Column(name = "f_modifier_user")
	private Long modifierUserId;

	@Version
	@Column(name = "n_version", nullable = false)
	private Integer version = 0;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DTaskInfo)) return false;

		DTaskInfo dTaskInfo = (DTaskInfo) o;

		return !(getId() != null ? !getId().equals(dTaskInfo.getId()) : dTaskInfo.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}
}
