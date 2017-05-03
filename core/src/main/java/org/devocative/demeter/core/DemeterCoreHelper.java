package org.devocative.demeter.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.core.xml.XModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.*;
import java.util.Collection;

public class DemeterCoreHelper {
	private static final Logger logger = LoggerFactory.getLogger(DemeterCoreHelper.class);

	public static void applySQLSchemas(Collection<XModule> xModules) {
		Connection connection = null;
		try {
			Class.forName(ConfigUtil.getString(true, "dmt.db.driver"));

			connection = DriverManager.getConnection(
				ConfigUtil.getString(true, "dmt.db.url"),
				ConfigUtil.getString(true, "dmt.db.username"),
				ConfigUtil.getString(true, "dmt.db.password"));

			logger.info("Create database connection!");

			for (XModule xModule : xModules) {
				logger.info("Process DModule: {}", xModule.getShortName());

				String db = "oracle"; //TODO
				String module = xModule.getShortName().toLowerCase();
				URL verUrl = DemeterCoreHelper.class.getResource(String.format("/sql/%s_versions.txt", module));
				if (verUrl != null) {
					LineIterator iterator = IOUtils.lineIterator(verUrl.openStream(), "UTF-8");
					while (iterator.hasNext()) {
						String version = iterator.next().trim();

						if (version.length() == 0) {
							continue;
						}

						long count = 0;
						try {
							Statement cnt = connection.createStatement();
							ResultSet resultSet = cnt.executeQuery(String.format("select count(1) from z_dmt_sql_apply where c_module='%s' and c_version='%s'", module, version));
							if (resultSet.next()) {
								count = resultSet.getLong(1);
							}
						} catch (SQLException e) {
							count = 0;
						}

						if (count == 0) {
							String sqlFile = String.format("/sql/%s_%s_%s.sql", module, db, version);
							URL sqlUrl = DemeterCore.class.getResource(sqlFile);
							if (sqlUrl != null) {
								logger.info("Applying SQL file: DModule=[{}] Version=[{}] URL=[{}]", module, version, sqlUrl);

								String sql = IOUtils.toString(sqlUrl, "UTF-8");
								sql = sql.replaceAll("[-][-].+?[\r]?[\n]", "");

								String[] statements = sql.split("[;]");
								for (String statement : statements) {
									statement = statement.trim();
									if (!statement.isEmpty()) {
										try {
											Statement st = connection.createStatement();
											st.execute(statement);
											st.close();
										} catch (SQLException e) {
											logger.error("Executing SQL: {}", statement, e);
											throw e;
										}
									}
								}

								PreparedStatement ps = connection.prepareStatement("insert into z_dmt_sql_apply(c_module,c_version,c_file,d_apply) VALUES(?,?,?,?)");
								ps.setString(1, xModule.getShortName().toLowerCase());
								ps.setString(2, version);
								ps.setString(3, sqlFile);
								ps.setDate(4, new Date(new java.util.Date().getTime()));
								ps.executeUpdate();
							} else {
								logger.warn("SQL file not found: DModule=[{}] Version=[{}]", module, version);
							}
						} else {
							logger.info("SQL file already applied: DModule=[{}] Version=[{}]", module, version);
						}
					}
				} else {
					logger.warn("Versions file not found for DModule [{}]", module);
				}
			}


		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("Closing connection", e);
				}
			}
		}
	}
}