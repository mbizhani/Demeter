package org.devocative.demeter.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "z_dmt_sql_apply")
public class ZSqlApply implements Serializable {
	private static final long serialVersionUID = -1571938208294343383L;

	@EmbeddedId
	private ZSqlApplyId id;

	@Column(name = "c_file", nullable = false)
	private String file;

	@Column(name = "d_apply", columnDefinition = "date", nullable = false)
	private Date apply;

	// ------------------------------

	public ZSqlApplyId getId() {
		return id;
	}

	public void setId(ZSqlApplyId id) {
		this.id = id;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Date getApply() {
		return apply;
	}

	public void setApply(Date apply) {
		this.apply = apply;
	}

	// ---------------

	public String getModule() {
		return getId() != null ? getId().getModule() : null;
	}

	public void setModule(String module) {
		if (getId() == null) {
			setId(new ZSqlApplyId());
		}
		getId().setModule(module);
	}

	public String getVersion() {
		return getId() != null ? getId().getVersion() : null;
	}

	public void setVersion(String version) {
		if (getId() == null) {
			setId(new ZSqlApplyId());
		}
		getId().setVersion(version);
	}

	// ------------------------------

	@Embeddable
	public static class ZSqlApplyId implements Serializable {
		private static final long serialVersionUID = 4839089414900531992L;

		@Column(name = "c_version", nullable = false)
		private String version;

		@Column(name = "c_module", nullable = false)
		private String module;

		// ------------------------------

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getModule() {
			return module;
		}

		public void setModule(String module) {
			this.module = module;
		}

		// ---------------

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ZSqlApplyId)) return false;

			ZSqlApplyId that = (ZSqlApplyId) o;

			if (getModule() != null ? !getModule().equals(that.getModule()) : that.getModule() != null) return false;
			return !(getVersion() != null ? !getVersion().equals(that.getVersion()) : that.getVersion() != null);

		}

		@Override
		public int hashCode() {
			int result = getModule() != null ? getModule().hashCode() : 0;
			result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
			return result;
		}
	}
}
