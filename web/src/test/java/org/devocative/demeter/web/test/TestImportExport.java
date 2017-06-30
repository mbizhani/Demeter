package org.devocative.demeter.web.test;

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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

//		tie.s01CheckExport();
		tie.s02CheckImport();
	}

	public void s01CheckExport() throws SQLException, FileNotFoundException {
		DemeterCore.init(TestImportExport.class.getResourceAsStream("/config_OraDbExp.properties"));

		IPersistorService persistorService = DemeterCore.getApplicationContext().getBean(IPersistorService.class);
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

		DemeterCore.shutdown();
	}

	public void s02CheckImport() throws SQLException, FileNotFoundException {
		DemeterCore.init(TestImportExport.class.getResourceAsStream("/config_OraDbImp.properties"));

		IPersistorService persistorService = DemeterCore.getApplicationContext().getBean(IPersistorService.class);
		Connection sqlConnection = persistorService.createSqlConnection();
		sqlConnection.setAutoCommit(false);

		Object userId = DemeterCore.getApplicationContext().getBean(ISecurityService.class).getCurrentUser().getUserId();
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

		DemeterCore.shutdown();
	}

	// ------------------------------

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