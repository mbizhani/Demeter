package org.devocative.demeter.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_dmt_d_page")
public class DPageInfo implements ICreationDate, IModificationDate {
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

	@Column(name = "c_type", nullable = false, unique = true)
	private String type;

	@Column(name = "c_module", nullable = false)
	private String module;

	@Column(name = "c_base_uri", nullable = false, unique = true)
	private String baseUri;

	@Column(name = "b_enabled", nullable = false)
	private Boolean enabled = true;

	//----------------------------- CREATE / MODIFY

	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

	@Column(name = "d_modification", columnDefinition = "date")
	private Date modificationDate;

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

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
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

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
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
		if (!(o instanceof DPageInfo)) return false;

		DPageInfo dPageInfo = (DPageInfo) o;

		return !(getId() != null ? !getId().equals(dPageInfo.getId()) : dPageInfo.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}
}
