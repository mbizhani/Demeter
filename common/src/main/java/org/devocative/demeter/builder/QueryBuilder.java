package org.devocative.demeter.builder;

import org.devocative.demeter.iservice.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QueryBuilder {
	private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

	private StringBuilder selectBuilder = new StringBuilder();
	private StringBuilder clauseBuilder = new StringBuilder();
	private Map<String, Object> params = new HashMap<String, Object>();
	private Map<String, QueryBuilder> subQueries = new HashMap<String, QueryBuilder>();
	private Map<String, Integer> subQueriesParamIndex = new HashMap<String, Integer>();
	private int subQueriesIndex = 1;
	private Map<String, String> join = new LinkedHashMap<String, String>(); // alias -> join expression
	private Map<String, String> from = new HashMap<String, String>(); // alias -> entity name
	private String orderBy;
	private String groupBy;
	private String having;

	private IPersistorService persistorService;

	public QueryBuilder(IPersistorService persistorService, String select) {
		this.persistorService = persistorService;
		selectBuilder.append(select);
	}

	public QueryBuilder addFrom(String entity, String alias) {
		if (!from.containsKey(alias))
			from.put(alias, entity);
		return this;
	}

	public QueryBuilder addParam(String name, Object value) {
		params.put(name, value);
		return this;
	}

	public QueryBuilder addParam(String name, Calendar value) {
		java.sql.Date sqlDate = new java.sql.Date(value.getTimeInMillis());
		params.put(name, sqlDate);
		return this;
	}

	public QueryBuilder addParam(String name, Date value) {
		java.sql.Date sqlDate = new java.sql.Date(value.getTime());
		params.put(name, sqlDate);
		return this;
	}

	public QueryBuilder addParams(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}

	public QueryBuilder addClause(String clause) {
		clauseBuilder.append(" ").append(clause);
		return this;
	}

	public QueryBuilder appendSelect(String select) {
		selectBuilder.append(" ").append(select);
		return this;
	}

	public QueryBuilder addJoin(String alias, String joinExpr) {
		if (!join.containsKey(alias))
			join.put(alias, joinExpr);
		return this;
	}

	public QueryBuilder addSubQueries(String name, QueryBuilder builder) {
		subQueries.put(name, builder);
		subQueriesParamIndex.put(name, subQueriesIndex++);
		return this;
	}

	public QueryBuilder setOrderBy(String order) {
		this.orderBy = order;
		return this;
	}

	public QueryBuilder setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		return this;
	}

	public QueryBuilder setHaving(String having) {
		this.having = having;
		return this;
	}

	private String buildQueryString(boolean applyOrder) {
		String finalQuery;

		String q = selectBuilder.toString();

		StringBuilder concatAllBuilder = new StringBuilder();
		concatAllBuilder.append(q);

		if (from.size() > 0) {
			concatAllBuilder.append(" from ");
			int noOfFromPart = 1;
			for (Map.Entry<String, String> entry : from.entrySet()) {
				concatAllBuilder.append(entry.getValue()).append(" ").append(entry.getKey());
				if (noOfFromPart < from.size())
					concatAllBuilder.append(",");
				noOfFromPart++;
			}
		}

		if (join.size() > 0) {
			for (String joinExpr : join.values())
				concatAllBuilder.append(" ").append(joinExpr).append(" ");
		}

		if (clauseBuilder.length() > 0)
			concatAllBuilder.append(" where 1=1 ").append(clauseBuilder.toString());

		if (groupBy != null)
			concatAllBuilder.append(" group by ").append(groupBy);

		if (having != null)
			concatAllBuilder.append(" having ").append(having);

		if (applyOrder && orderBy != null)
			concatAllBuilder.append(" order by ").append(orderBy);

		finalQuery = concatAllBuilder.toString();

		if (subQueries.size() > 0) {
			for (Map.Entry<String, QueryBuilder> subQueryEntry : subQueries.entrySet()) {
				QueryBuilder builder = subQueryEntry.getValue();
				String query = builder.buildQueryString(false);

				Integer subQueryIndex = subQueriesParamIndex.get(subQueryEntry.getKey());
				for (Map.Entry<String, Object> paramEntry : builder.params.entrySet())
					query = query.replace(":" + paramEntry.getKey(), ":" + paramEntry.getKey() + subQueryIndex);

				finalQuery = finalQuery.replace("#" + subQueryEntry.getKey(), "(" + query + ")");


			}
		}

		logger.debug("HQLBuilder, final: {}", finalQuery);

		return finalQuery;
	}

}
