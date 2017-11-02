package org.devocative.demeter.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DemeterCoreHelper {
	private static final Logger logger = LoggerFactory.getLogger(DemeterCoreHelper.class);

	public static void initDatabase(List<String> modules, boolean force) {
		try (Connection connection = createConnection()) {
			List<DbDiffVO> diffs = findDiffs(connection, modules, PaginationPlugin.findDatabaseType(connection).toString().toLowerCase());
			logger.info("Database Found Diff(s): no = [{}]", diffs.size());

			if (!diffs.isEmpty()) {
				if (force || ConfigUtil.getBoolean(DemeterConfigKey.DatabaseDiffAuto)) {
					applyDiffs(connection, diffs);
				} else {
					throw new DSystemException("Database Diff Found: Numbers=" + diffs.size());
				}
			}
		} catch (IOException | SQLException e) {
			throw new DSystemException(e);
		}
	}

	public static List<DbDiffVO> getDbDiffs(List<String> modules) {
		try (Connection connection = createConnection()) {
			return findDiffs(connection, modules, PaginationPlugin.findDatabaseType(connection).toString().toLowerCase());
		} catch (IOException | SQLException e) {
			throw new DSystemException(e);
		}
	}

	public static void applyDbDiffs(List<DbDiffVO> dbDiffVOs) {
		try (Connection connection = createConnection()) {
			applyDiffs(connection, dbDiffVOs);
		} catch (SQLException e) {
			throw new DSystemException(e);
		}
	}

	// ------------------------------

	private static void applyDiffs(Connection connection, List<DbDiffVO> diffs) throws SQLException {
		for (DbDiffVO diff : diffs) {
			logger.info("Database Apply Diff: [{}]", diff.getFile());

			String sql = diff.getSql();
			sql = sql.replaceAll("[-][-].+?[\r]?[\n]", "");
			String[] statements = sql.split("[;]");
			for (String statement : statements) {
				statement = statement.trim();
				if (!statement.isEmpty()) {
					Statement st = connection.createStatement();
					st.execute(statement);
					st.close();
				}
			}

			PreparedStatement select = connection.prepareStatement("select count(1) from z_dmt_sql_apply where c_module=? and c_version=?");
			select.setString(1, diff.getModule());
			select.setString(2, diff.getVersion());
			ResultSet rs = select.executeQuery();
			rs.next();
			long cnt = rs.getLong(1);
			if (cnt == 0) {
				logger.warn("Insert SQL Apply: module=[{}] version=[{}]", diff.getModule(), diff.getVersion());
				PreparedStatement insert = connection.prepareStatement("insert into z_dmt_sql_apply(c_module,c_version,c_file,d_apply) VALUES(?,?,?,?)");
				insert.setString(1, diff.getModule());
				insert.setString(2, diff.getVersion());
				insert.setString(3, diff.getFile());
				insert.setDate(4, new Date(new java.util.Date().getTime()));
				insert.executeUpdate();
			}
		}
	}

	private static List<DbDiffVO> findDiffs(Connection connection, List<String> modules, String dbType) throws IOException, SQLException {
		List<DbDiffVO> result = new ArrayList<>();
		Map<String, List<String>> applied = findApplied(connection);

		for (String module : modules) {
			module = module.toLowerCase();

			URL verUrl = DemeterCoreHelper.class.getResource(String.format("/sql/%s_versions.txt", module));
			if (verUrl != null) {
				LineIterator iterator = IOUtils.lineIterator(verUrl.openStream(), "UTF-8");
				while (iterator.hasNext()) {
					String version = iterator.next().trim();
					if (!version.isEmpty() && (!applied.containsKey(module) || !applied.get(module).contains(version))) {
						String sqlFile = String.format("/sql/%s_%s_%s.sql", module, dbType, version);
						URL sqlUrl = DemeterCore.class.getResource(sqlFile);
						if (sqlUrl != null) {
							String sql = IOUtils.toString(sqlUrl, "UTF-8");

							sql += "\n\n-- Add SQL_Apply\n";
							sql += String.format("insert into z_dmt_sql_apply(c_module,c_version,c_file,d_apply) VALUES('%s','%s','%s',sysdate);",
								module, version, sqlFile);
							result.add(new DbDiffVO(module, version, sqlFile, sql));
						} else {
							throw new DSystemException("SQL file not found: " + sqlFile);
						}
					}
				}
				iterator.close();
			} else {
				throw new DSystemException("'versions.txt' File Not Found: " + module);
			}
		}
		return result;
	}

	private static Map<String, List<String>> findApplied(Connection connection) {
		Map<String, List<String>> applied = new LinkedHashMap<>();

		try (Statement st = connection.createStatement()) {
			ResultSet rs = st.executeQuery("select c_module, c_version from z_dmt_sql_apply");
			while (rs.next()) {
				String module = rs.getString("c_module");
				String version = rs.getString("c_version");

				if (!applied.containsKey(module)) {
					applied.put(module, new ArrayList<>());
				}

				applied.get(module).add(version);
			}
		} catch (SQLException e) {
			logger.warn("DemeterCoreHelper.findApplied: {}", e.getMessage());
		}

		return applied;
	}

	private static Connection createConnection() {
		try {
			Class.forName(ConfigUtil.getString(true, "dmt.db.driver"));

			return DriverManager.getConnection(
				ConfigUtil.getString(true, "dmt.db.url"),
				ConfigUtil.getString(true, "dmt.db.username"),
				ConfigUtil.getString("dmt.db.password", ""));
		} catch (Exception e) {
			throw new DSystemException(e);
		}
	}
}