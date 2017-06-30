package org.devocative.demeter.ei;

import org.devocative.adroit.sql.NamedParameterStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.*;

public class Importer {
	private static final Logger logger = LoggerFactory.getLogger(Importer.class);

	// ------------------------------

	private String tableName;
	private NamedParameterStatement insertNps;
	private NamedParameterStatement updateNps;
	private Map<String, String> insertKeysToColumns;
	private Map<String, String> updateAll_KeysToColumns;
	private int noOfInserts = 0, noOfUpdates = 0;

	// ------------------------------

	Importer(Connection connection, String tableName, List<String> insertColumns) {
		this(connection, tableName, insertColumns, Collections.emptyList(), Collections.emptyList());
	}

	Importer(Connection connection, String tableName, List<String> insertColumns, List<String> updateColumns, List<String> updateWhereColumns) {
		this.tableName = tableName;

		insertKeysToColumns = processColumns(insertColumns);
		Map<String, String> updateSet_KeysToColumns = processColumns(updateColumns);
		Map<String, String> updateWhere_KeysToColumns = processColumns(updateWhereColumns);

		updateAll_KeysToColumns = new HashMap<>();
		updateAll_KeysToColumns.putAll(updateSet_KeysToColumns);
		updateAll_KeysToColumns.putAll(updateWhere_KeysToColumns);

		List<String> columns = new ArrayList<>(insertKeysToColumns.values());

		StringBuilder builder = new StringBuilder();
		builder
			.append("insert into ")
			.append(tableName)
			.append(" (")
			.append(columns.get(0));
		for (int i = 1; i < columns.size(); i++) {
			builder
				.append(", ")
				.append(columns.get(i));
		}

		builder
			.append(") values (:")
			.append(columns.get(0));
		for (int i = 1; i < columns.size(); i++) {
			builder.append(", :").append(columns.get(i));
		}

		builder.append(")");

		logger.info("Importer: table=[{}] insertSQL=[{}]", tableName, builder.toString());

		insertNps = new NamedParameterStatement(connection, builder.toString());
		insertNps.setDateClassReplacement(Date.class);

		if (updateSet_KeysToColumns.size() > 0) {
			columns = new ArrayList<>(updateSet_KeysToColumns.values());

			builder = new StringBuilder();
			builder
				.append("update ")
				.append(tableName)
				.append(" set ")
				.append(columns.get(0))
				.append(" = :")
				.append(columns.get(0));
			for (int i = 1; i < columns.size(); i++) {
				builder
					.append(", ")
					.append(columns.get(i))
					.append(" = :")
					.append(columns.get(i));
			}

			columns = new ArrayList<>(updateWhere_KeysToColumns.values());
			builder
				.append(" where ")
				.append(columns.get(0))
				.append(" = :")
				.append(columns.get(0));
			for (int i = 1; i < columns.size(); i++) {
				builder
					.append(" and ")
					.append(columns.get(i))
					.append(" = :")
					.append(columns.get(i));
			}

			logger.info("Importer: table=[{}] updateSQL=[{}]", tableName, builder.toString());

			updateNps = new NamedParameterStatement(connection, builder.toString());
			updateNps.setDateClassReplacement(Date.class);
		}
	}

	// ------------------------------

	@SafeVarargs
	public final void addInsert(Map<String, Object>... maps) throws SQLException {
		if (insertNps != null) {
			addAction(insertNps, insertKeysToColumns, maps);
			noOfInserts++;
		} else {
			throw new RuntimeException("No insert for importer: " + tableName);
		}
	}

	@SafeVarargs
	public final void addUpdate(Map<String, Object>... maps) throws SQLException {
		if (updateNps != null) {
			addAction(updateNps, updateAll_KeysToColumns, maps);
			noOfUpdates++;
		} else {
			throw new RuntimeException("No update for importer: " + tableName);
		}
	}

	public void executeBatch() throws SQLException {
		if (insertNps != null) {
			insertNps.executeBatch();
		} else {
			noOfInserts = -1;
		}

		if (updateNps != null) {
			updateNps.executeBatch();
		} else {
			noOfUpdates = -1;
		}

		logger.info("Importer ExecBatch: table=[{}] inserts=[{}] updates=[{}]", tableName, noOfInserts, noOfUpdates);
	}

	// ------------------------------

	private void addAction(NamedParameterStatement nps, Map<String, String> keysToColumns, Map<String, Object>[] maps) throws SQLException {
		Map<String, Object> params = new HashMap<>();

		for (Map.Entry<String, String> entry : keysToColumns.entrySet()) {
			for (Map<String, Object> map : maps) {
				if (map.containsKey(entry.getKey())) {
					params.put(entry.getValue(), map.get(entry.getKey()));
					break;
				}
			}
		}

		if (params.size() < keysToColumns.size()) {
			Set<String> requiredParams = keysToColumns.keySet();
			requiredParams.removeAll(params.keySet());
			throw new RuntimeException(String.format("Insufficient params: not sent params for query= %s", requiredParams));
		}

		nps.setParameters(params);
		nps.addBatch();
	}

	private Map<String, String> processColumns(List<String> columns) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String col : columns) {
			String[] split = col.split("[:]");
			if (split.length == 1) {
				result.put(split[0], split[0]);
			} else if (split.length == 2) {
				result.put(split[0], split[1]);
			} else {
				throw new RuntimeException("Invalid column: " + col);
			}
		}
		return result;
	}
}
