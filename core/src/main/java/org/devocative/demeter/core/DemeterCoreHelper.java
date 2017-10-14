package org.devocative.demeter.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.sql.plugin.PaginationPlugin;
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

			if (force || ConfigUtil.getBoolean(DemeterConfigKey.DatabaseDiffAuto)) {
				applyDiffs(connection, diffs);
			} else {
				throw new RuntimeException("Database Diff Found");
			}
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<DbDiffVO> getDbDiffs(List<String> modules) {
		try (Connection connection = createConnection()) {
			return findDiffs(connection, modules, PaginationPlugin.findDatabaseType(connection).toString().toLowerCase());
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void applyDbDiffs(List<DbDiffVO> dbDiffVOs) {
		try (Connection connection = createConnection()) {
			applyDiffs(connection, dbDiffVOs);
		} catch (SQLException e) {
			throw new RuntimeException(e);
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

			PreparedStatement ps = connection.prepareStatement("insert into z_dmt_sql_apply(c_module,c_version,c_file,d_apply) VALUES(?,?,?,?)");
			ps.setString(1, diff.getModule());
			ps.setString(2, diff.getVersion());
			ps.setString(3, diff.getFile());
			ps.setDate(4, new Date(new java.util.Date().getTime()));
			ps.executeUpdate();
		}
	}

	private static List<DbDiffVO> findDiffs(Connection connection, List<String> modules, String dbType) throws IOException, SQLException {
		List<DbDiffVO> result = new ArrayList<>();
		Map<String, List<String>> applied = findApplied(connection);

		for (String module : modules) {
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
							result.add(new DbDiffVO(module, version, sqlFile, sql));
						} else {
							throw new RuntimeException("SQL file not found: " + sqlFile);
						}
					}
				}
				iterator.close();
			} else {
				throw new RuntimeException("'versions.txt' File Not Found: " + module);
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
			logger.error("DemeterCoreHelper.findApplied: {}", e.getMessage());
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
			throw new RuntimeException(e);
		}
	}
}