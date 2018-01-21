package org.devocative.demeter.test;

import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.demeter.core.DemeterCore;
import org.devocative.demeter.ei.ExportImportHelper;
import org.devocative.demeter.ei.Importer;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class TestImportExport {
	private static final Logger logger = LoggerFactory.getLogger(TestImportExport.class);

/*

delete from mt_mts_dataview_group;
delete from mt_mts_report_group;
delete from t_mts_data_group;
delete from t_mts_report;
delete from t_mts_data_src_rel;
delete from t_mts_data_view;
delete from t_mts_data_src;
delete from t_mts_db_conn;
delete from t_mts_db_conn_grp;
delete from t_mts_cfg_lob;

commit;

*/

	public static void main(String[] args) throws FileNotFoundException, SQLException {
		TestImportExport tie = new TestImportExport();

		//tie.s01CheckExport();
		//tie.s02CheckImport();

		tie.validateExpImp();
	}

	private void validateExpImp() throws SQLException {
		Connection master = createConnection("/config_OraDbExp.properties");
		Connection slave = createConnection("/config_OraDbImp.properties");

		String q =
			"select " +
				"  ds.id, " +
				"  ds.c_name, " +
				"  ds.c_title, " +
				"  ds.n_version, " +
				"  ds.c_title_field, " +
				"  ds.c_key_field, " +
				"  ds.c_self_rel_pointer_field, " +
				"  ds.f_config, " +
				"  cfg.c_value " +
				"from t_mts_data_src ds " +
				"join t_mts_cfg_lob cfg on cfg.id = ds.f_config";

		Map<Object, Map<String, Object>> mast = create(master, q, "id");

		Map<Object, Map<String, Object>> sl = create(slave, q, "id");

		for (Map.Entry<Object, Map<String, Object>> entry : mast.entrySet()) {
			Map<String, Object> m = entry.getValue();
			Map<String, Object> s = sl.get(entry.getKey());

			if (s == null) {
				System.out.println("ERROR");
			} else {
				if (!m.get("c_value").equals(s.get("c_value"))) {
					System.out.println("ERROR: " + entry.getKey());
					System.out.println("MASTER: \n" + m.get("c_value"));
					System.out.println("SLAVE: \n" + s.get("c_value"));
					System.out.println("++++++++++++++++++++++++++++++++++++++++++");
				}
			}
		}

		master.close();
		slave.close();
	}

	private Connection createConnection(String file) {
		ConfigUtil.load(TestImportExport.class.getResourceAsStream(file));

		try {
			Class.forName(ConfigUtil.getString(true, "dmt.db.driver"));

			return DriverManager.getConnection(
				ConfigUtil.getString(true, "dmt.db.url"),
				ConfigUtil.getString(true, "dmt.db.username"),
				ConfigUtil.getString(true, "dmt.db.password"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<Object, Map<String, Object>> create(Connection conn, String sql, String id) throws SQLException {
		NamedParameterStatement nps = new NamedParameterStatement(conn, sql);

		ResultSet rs = nps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();

		Map<Object, Map<String, Object>> rows = new LinkedHashMap<>();
		while (rs.next()) {
			Map<String, Object> row = new LinkedHashMap<>();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String column = metaData.getColumnName(i).toLowerCase();
				Object value = findCellValue(rs, column, metaData.getColumnType(i));
				row.put(column, value);
			}

			rows.put(row.get(id), row);
		}

		return rows;
	}

	private Object findCellValue(ResultSet rs, String column, int type) throws SQLException {
		Object value;
		switch (type) {
			case Types.DATE:
				value = rs.getDate(column);
				break;
			case Types.TIME:
				value = rs.getTime(column);
				break;
			case Types.TIMESTAMP:
				value = rs.getTimestamp(column);
				break;
			case Types.CLOB:
				value = rs.getString(column);
				break;
			default:
				value = rs.getObject(column);
		}

		return value;
	}
	// ------------------------------

	public void s01CheckExport() throws SQLException, FileNotFoundException {
		DemeterCore.get().init(/*TestImportExport.class.getResourceAsStream("/config_OraDbExp.properties")*/);

		IPersistorService persistorService = DemeterCore.get().getApplicationContext().getBean(IPersistorService.class);
		Connection sqlConnection = persistorService.createSqlConnection();

		ExportImportHelper helper = new ExportImportHelper(sqlConnection);

		helper.exportBySql("dbConnGrp",
			"select " +
				"  grp.id, " +
				"  grp.c_name, " +
				"  grp.c_driver, " +
				"  grp.c_test_query, " +
				"  grp.f_config, " +
				"  grp.n_version, " +
				"  cfg.c_value " +
				"from t_mts_db_conn_grp grp " +
				"join t_mts_cfg_lob cfg on cfg.id = grp.f_config " +
				"where grp.c_name='MidRP'");

		helper.exportBySql("dataSource",
			"select " +
				"  ds.id, " +
				"  ds.c_name, " +
				"  ds.c_title, " +
				"  ds.n_version, " +
				"  ds.c_title_field, " +
				"  ds.c_key_field, " +
				"  ds.c_self_rel_pointer_field, " +
				"  ds.f_config, " +
				"  cfg.c_value " +
				"from t_mts_data_src ds " +
				"join t_mts_cfg_lob cfg on cfg.id = ds.f_config");

		helper.exportBySql("dataView",
			"select " +
				"  dv.id, " +
				"  dv.c_name, " +
				"  dv.c_title, " +
				"  dv.f_data_src, " +
				"  dv.f_config, " +
				"  dv.n_version, " +
				"  cfg.c_value " +
				"from t_mts_data_view dv " +
				"join t_mts_cfg_lob cfg on cfg.id = dv.f_config");

		helper.exportBySql("dataSrcRel",
			"select " +
				"  id, " +
				"  b_deleted, " +
				"  f_src_datasrc, " +
				"  c_src_ptr_field, " +
				"  f_tgt_datasrc, " +
				"  n_version " +
				"from t_mts_data_src_rel " +
				"where b_deleted=0");

		helper.exportBySql("report",
			"select " +
				"  id, " +
				"  c_title, " +
				"  c_config, " +
				"  f_data_view, " +
				"  n_version " +
				"from t_mts_report");

		helper.exportBySql("group",
			"select " +
				"  id, " +
				"  c_name, " +
				"  n_version " +
				"from t_mts_data_group");

		helper.exportBySql("group_report",
			"select " +
				"  f_report, " +
				"  f_group " +
				"from mt_mts_report_group");

		helper.exportBySql("group_dataView",
			"select " +
				"  f_data_view, " +
				"  f_group " +
				"from mt_mts_dataview_group");

		helper.exportTo(new FileOutputStream("a.xml"));

		sqlConnection.close();

		DemeterCore.get().shutdown();
	}

	public void s02CheckImport() throws SQLException, FileNotFoundException {
		DemeterCore.get().init(/*TestImportExport.class.getResourceAsStream("/config_OraDbImp.properties")*/);

		IPersistorService persistorService = DemeterCore.get().getApplicationContext().getBean(IPersistorService.class);
		Connection sqlConnection = persistorService.createSqlConnection();
		sqlConnection.setAutoCommit(false);

		Object userId = DemeterCore.get().getApplicationContext().getBean(ISecurityService.class).getCurrentUser().getUserId();
		logger.info("Current User: id=[{}]", userId);

		ExportImportHelper helper = new ExportImportHelper(sqlConnection);
		helper.importFrom(new FileInputStream("a.xml"));

		Date now = new Date();
		Map<String, Object> other = new HashMap<>();
		other.put("d_creation", now);
		other.put("f_creator_user", userId);
		other.put("d_modification", now);
		other.put("f_modifier_user", userId);

		helper.setCommonData(other);

		dbConn(helper);

		dataSrc(helper);
		dataSrcRel(helper);
		dataView(helper);
		report(helper);

		group(helper);
		group_report(helper);
		group_dataView(helper);

		sqlConnection.commit();

		DemeterCore.get().shutdown();
	}

	// ---------------

	private void dbConn(ExportImportHelper helper) throws SQLException {
		Map<Object, Object> currentDbConnGrp = helper.selectAsMap("select id, n_version from t_mts_db_conn_grp");
		logger.info("Current DbConnGrp: size=[{}]", currentDbConnGrp.size());

		Importer dbConnGrp = helper.createImporter("t_mts_db_conn_grp",
			Arrays.asList("id", "n_version", "c_name", "c_driver", "c_test_query", "f_config", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_name", "c_driver", "c_test_query", "f_config", "d_modification", "f_modifier_user"),
			Arrays.asList("id")
		);

		Importer dbConnGrpLob = helper.createImporter("t_mts_cfg_lob",
			Arrays.asList("f_config:id", "n_version", "c_value", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_value", "d_modification", "f_modifier_user"),
			Arrays.asList("f_config:id")
		);

		helper.merge("dbConnGrp", "id", "n_version", currentDbConnGrp, dbConnGrpLob, dbConnGrp);


		Object f_connection = helper.selectFirstCell("select id from t_mts_db_conn where c_name='default'");
		if (f_connection == null) {
			logger.info("No default db connection, creating one!");
			f_connection = 0; //NOTE: not using sequence, supposing related sequence has been started form one!
			Object midRPId = helper.selectFirstCell("select id from t_mts_db_conn_grp where c_name='MidRP'");

			Map<String, Object> defaultDbConn = new HashMap<>();
			defaultDbConn.put("id", f_connection);
			defaultDbConn.put("c_name", "default");
			defaultDbConn.put("c_username", "default");
			defaultDbConn.put("f_group", midRPId);
			defaultDbConn.put("n_version", 1);

			Importer db_conn = helper.createImporter("t_mts_db_conn",
				Arrays.asList("id", "c_name", "c_username", "f_group", "f_group", "n_version", "d_creation", "f_creator_user"));
			db_conn.addInsert(defaultDbConn, helper.getCommonData());
			db_conn.executeBatch();
		}
		logger.info("Default db connection id=[{}]", f_connection);
		helper.getCommonData().put("f_connection", f_connection);
	}

	private void dataSrc(ExportImportHelper helper) throws SQLException {
		Map<Object, Object> currentDataSrc = helper.selectAsMap("select id, n_version from t_mts_data_src");
		logger.info("Current DataSrc: size=[{}]", currentDataSrc.size());

		Importer dataSrc = helper.createImporter("t_mts_data_src",
			Arrays.asList("id", "n_version", "c_name", "c_title", "c_title_field", "c_key_field", "c_self_rel_pointer_field", "f_config", "f_connection", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_name", "c_title", "c_title_field", "c_key_field", "c_self_rel_pointer_field", "f_connection", "d_modification", "f_modifier_user"),
			Arrays.asList("id")
		);

		Importer dataSrcLob = helper.createImporter("t_mts_cfg_lob",
			Arrays.asList("f_config:id", "n_version", "c_value", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_value", "d_modification", "f_modifier_user"),
			Arrays.asList("f_config:id")
		);

		helper.merge("dataSource", "id", "n_version", currentDataSrc, dataSrcLob, dataSrc);
	}

	private void dataSrcRel(ExportImportHelper helper) throws SQLException {
		helper.clearTable("t_mts_data_src_rel");

		Importer dataSrcRel = helper.createImporter("t_mts_data_src_rel",
			Arrays.asList("id", "n_version", "b_deleted", "f_src_datasrc", "c_src_ptr_field", "f_tgt_datasrc", "d_creation", "f_creator_user")
		);

		for (Map<String, Object> row : helper.getDataSets().get("dataSrcRel")) {
			dataSrcRel.addInsert(row, helper.getCommonData());
		}

		dataSrcRel.executeBatch();
	}

	private void dataView(ExportImportHelper helper) throws SQLException {
		Map<Object, Object> currentDataView = helper.selectAsMap("select id, n_version from t_mts_data_view");
		logger.info("Current DataView: size=[{}]", currentDataView.size());

		Importer dataView = helper.createImporter("t_mts_data_view",
			Arrays.asList("id", "n_version", "c_name", "c_title", "f_config", "f_data_src", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_name", "c_title", "d_modification", "f_modifier_user"),
			Arrays.asList("id")
		);

		Importer dataViewLob = helper.createImporter("t_mts_cfg_lob",
			Arrays.asList("f_config:id", "n_version", "c_value", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_value", "d_modification", "f_modifier_user"),
			Arrays.asList("f_config:id")
		);

		helper.merge("dataView", "id", "n_version", currentDataView, dataViewLob, dataView);
	}

	private void report(ExportImportHelper helper) throws SQLException {
		Map<Object, Object> currentReport = helper.selectAsMap("select id, n_version from t_mts_report");
		logger.info("Current Report: size=[{}]", currentReport.size());

		Importer report = helper.createImporter("t_mts_report",
			Arrays.asList("id", "n_version", "c_title", "c_config", "f_data_view", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_title", "c_config", "f_data_view", "d_modification", "f_modifier_user"),
			Arrays.asList("id")
		);

		helper.merge("report", "id", "n_version", currentReport, report);
	}

	private void group(ExportImportHelper helper) throws SQLException {
		Map<Object, Object> currentGroup = helper.selectAsMap("select id, n_version from t_mts_data_group");
		logger.info("Current DataGroup: size=[{}]", currentGroup.size());

		Importer group = helper.createImporter("t_mts_data_group",
			Arrays.asList("id", "n_version", "c_name", "d_creation", "f_creator_user"),
			Arrays.asList("n_version", "c_name", "d_modification", "f_modifier_user"),
			Arrays.asList("id")
		);

		helper.merge("group", "id", "n_version", currentGroup, group);
	}

	private void group_report(ExportImportHelper helper) throws SQLException {
		helper.clearTable("mt_mts_report_group");

		Importer report = helper.createImporter("mt_mts_report_group",
			Arrays.asList("f_report", "f_group")
		);

		for (Map<String, Object> row : helper.getDataSets().get("group_report")) {
			report.addInsert(row);
		}

		report.executeBatch();
	}

	private void group_dataView(ExportImportHelper helper) throws SQLException {
		helper.clearTable("mt_mts_dataview_group");

		Importer report = helper.createImporter("mt_mts_dataview_group",
			Arrays.asList("f_data_view", "f_group")
		);

		for (Map<String, Object> row : helper.getDataSets().get("group_dataView")) {
			report.addInsert(row);
		}

		report.executeBatch();
	}
}