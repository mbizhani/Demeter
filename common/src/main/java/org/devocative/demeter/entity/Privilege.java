package org.devocative.demeter.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_dmt_privilege", uniqueConstraints = {
	@UniqueConstraint(name = "uk_dmt_privilege_name", columnNames = {"c_name"})
})
public class Privilege implements ICreationDate {
	private static final long serialVersionUID = -2351213011902835877L;

	@Id
	@GeneratedValue(generator = "dmt_privilege")
	@org.hibernate.annotations.GenericGenerator(name = "dmt_privilege", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			//@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "dmt_privilege")
		})
	private Long id;

	@Column(name = "c_name", nullable = false)
	private String name;

	// ---------------

	@Column(name = "d_creation", nullable = false, columnDefinition = "date")
	private Date creationDate;

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

	// ---------------

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	// ---------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Privilege)) return false;

		Privilege that = (Privilege) o;

		return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName();
	}
}
