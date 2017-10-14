package org.devocative.demeter.core;

import java.io.Serializable;

public class DbDiffVO implements Serializable {
	private static final long serialVersionUID = -6584078471294139607L;

	private String module;
	private String version;
	private String file;
	private String sql;

	// ------------------------------

	public DbDiffVO(String module, String version, String file, String sql) {
		this.module = module;
		this.version = version;
		this.file = file;
		this.sql = sql;
	}

	// ------------------------------

	public String getModule() {
		return module;
	}

	public String getVersion() {
		return version;
	}

	public String getFile() {
		return file;
	}

	public String getSql() {
		return sql;
	}
}
