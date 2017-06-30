package org.devocative.demeter.ei;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.sql.NamedParameterStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.*;

public class ExportImportHelper {
	private static final Logger logger = LoggerFactory.getLogger(ExportImportHelper.class);

	// ------------------------------

	private Connection connection;
	private Map<String, List<Map<String, Object>>> dataSets = new LinkedHashMap<>();
	private Map<String, Object> commonData = new HashMap<>();

	// ------------------------------

	public ExportImportHelper(Connection connection) {
		this.connection = connection;
	}

	// ------------------------------

	public Map<String, Object> getCommonData() {
		return commonData;
	}

	public ExportImportHelper setCommonData(Map<String, Object> commonData) {
		this.commonData.putAll(commonData);
		return this;
	}

	// ---------------

	public void exportBySql(String name, String sql) throws SQLException {
		exportBySql(name, sql, new HashMap<>());
	}

	public void exportBySql(String name, String sql, Map<String, Object> params) throws SQLException {
		logger.info("Exporting: name=[{}]", name);

		NamedParameterStatement nps = new NamedParameterStatement(connection, sql);
		nps.setParameters(params);

		ResultSet rs = nps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();

		List<Map<String, Object>> rows = new ArrayList<>();
		while (rs.next()) {
			Map<String, Object> row = new LinkedHashMap<>();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String column = metaData.getColumnName(i).toLowerCase();
				Object value = findCellValue(rs, column, metaData.getColumnType(i));
				row.put(column, value);
			}

			rows.add(row);
		}

		dataSets.put(name, rows);
		nps.close();

		logger.info("Exported: name=[{}] count=[{}]", name, rows.size());
	}

	public Importer createImporter(String tableName, List<String> insertColumns, List<String> updateColumns, List<String> columnsOfUpdateCondition) {
		logger.info("Creating Importer: table=[{}]", tableName);

		return new Importer(connection, tableName, insertColumns, updateColumns, columnsOfUpdateCondition);
	}

	public Importer createImporter(String tableName, List<String> insertColumns) {
		logger.info("Creating Importer: table=[{}]", tableName);

		return new Importer(connection, tableName, insertColumns);
	}

	public Map<Object, Object> selectAsMap(String sql) throws SQLException {
		return selectAsMap(sql, new HashMap<>());
	}

	public Map<Object, Object> selectAsMap(String sql, Map<String, Object> params) throws SQLException {
		NamedParameterStatement nps = new NamedParameterStatement(connection, sql);
		nps.setParameters(params);

		ResultSet rs = nps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();
		String col1 = metaData.getColumnName(1);
		int col1Type = metaData.getColumnType(1);
		String col2 = metaData.getColumnName(2);
		int col2Type = metaData.getColumnType(2);

		Map<Object, Object> result = new LinkedHashMap<>();
		while (rs.next()) {
			Object key = findCellValue(rs, col1, col1Type);
			Object value = findCellValue(rs, col2, col2Type);
			result.put(key, value);
		}

		nps.close();

		return result;
	}

	public Object selectFirstCell(String sql) throws SQLException {
		return selectFirstCell(sql, new HashMap<>());
	}

	public Object selectFirstCell(String sql, Map<String, Object> params) throws SQLException {
		NamedParameterStatement nps = new NamedParameterStatement(connection, sql);
		nps.setParameters(params);

		ResultSet rs = nps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();

		Object result = null;
		if (rs.next()) {
			String column = metaData.getColumnName(1).toLowerCase();
			result = findCellValue(rs, column, metaData.getColumnType(1));
		}

		nps.close();

		return result;
	}

	public Map<String, List<Map<String, Object>>> getDataSets() {
		return dataSets;
	}

	public void clearTable(String tableName) throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate("delete from " + tableName);
		statement.close();
	}

	public void merge(String dataKey, String idCol, String verCol, Map<Object, Object> currentData, Importer... imports) throws SQLException {
		for (Map<String, Object> row : dataSets.get(dataKey)) {
			Object id = row.get(idCol);
			Comparable<Object> ver = (Comparable) row.get(verCol);

			if (currentData.containsKey(id)) {
				Object currentVer = currentData.get(id);
				if (ver.compareTo(currentVer) > 0) {
					for (Importer importer : imports) {
						importer.addUpdate(row, commonData);
					}
				}
			} else {
				for (Importer importer : imports) {
					importer.addInsert(row, commonData);
				}
			}
		}

		for (Importer importer : imports) {
			importer.executeBatch();
		}
	}

	// ---------------

	public void exportTo(OutputStream stream) {
		XStream xStream = new XStream();
		xStream.toXML(dataSets, stream);

		logger.info("Exported to Stream: size=[{}] keys={}", dataSets.size(), dataSets.keySet());
	}

	public void importFrom(InputStream stream) {
		XStream xStream = new XStream();
		dataSets = (LinkedHashMap<String, List<Map<String, Object>>>) xStream.fromXML(stream);

		logger.info("Imported from Stream: size=[{}] keys={}", dataSets.size(), dataSets.keySet());
	}

	// ------------------------------

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
}
