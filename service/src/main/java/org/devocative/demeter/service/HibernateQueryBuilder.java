package org.devocative.demeter.service;

import org.devocative.demeter.DSystemException;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HibernateQueryBuilder implements IQueryBuilder {
	private static final Logger logger = LoggerFactory.getLogger(HibernateQueryBuilder.class);

	private StringBuilder selectBuilder = new StringBuilder();
	private StringBuilder whereClauseBuilder = new StringBuilder();
	private Map<String, Object> params = new HashMap<>();
	private Map<String, IQueryBuilder> subQueries = new HashMap<>();
	private Map<String, Integer> subQueriesParamIndex = new HashMap<>();
	private int subQueriesIndex = 1;
	private Map<String, String> join = new LinkedHashMap<>(); // alias -> join expression
	private Map<String, String> from = new HashMap<>(); // alias -> entity name
	private String orderBy;
	private String groupBy;
	private String having;

	private boolean sqlMode = false;
	private Query query;
	private LockOptions lockOptions;

	private HibernatePersistorService persistorService;

	public HibernateQueryBuilder(HibernatePersistorService persistorService) {
		this.persistorService = persistorService;
	}

	public IQueryBuilder setSqlMode(boolean sqlMode) {
		this.sqlMode = sqlMode;
		return this;
	}

	public IQueryBuilder addFrom(String entity, String alias) {
		if (!from.containsKey(alias)) {
			from.put(alias, entity);
		}
		return this;
	}

	@Override
	public IQueryBuilder addFrom(Class entity, String alias) {
		addFrom(entity.getName(), alias);
		return this;
	}

	public IQueryBuilder addParam(String name, Object value) {
		params.put(name, value);
		return this;
	}

	public IQueryBuilder addParam(String name, Calendar value) {
		java.sql.Date sqlDate = new java.sql.Date(value.getTimeInMillis());
		params.put(name, sqlDate);
		return this;
	}

	public IQueryBuilder addParam(String name, Date value) {
		java.sql.Date sqlDate = new java.sql.Date(value.getTime());
		params.put(name, sqlDate);
		return this;
	}

	public IQueryBuilder addParams(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}

	public IQueryBuilder addSelect(String selectClause) {
		selectBuilder.append(" ").append(selectClause);
		return this;
	}

	public IQueryBuilder addWhere(String whereClause) {
		whereClauseBuilder.append(" ").append(whereClause);
		return this;
	}

	public IQueryBuilder addJoin(String alias, String joinExpr) {
		if (!join.containsKey(alias)) {
			join.put(alias, joinExpr);
		} else {
			throw new DSystemException("Duplicate join alias: " + alias);
		}
		return this;
	}

	public IQueryBuilder addSubQueries(String name, IQueryBuilder builder) {
		subQueries.put(name, builder);
		subQueriesParamIndex.put(name, subQueriesIndex++);
		return this;
	}

	public IQueryBuilder setOrderBy(String order) {
		this.orderBy = order;
		return this;
	}

	public IQueryBuilder setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		return this;
	}

	public IQueryBuilder setHaving(String having) {
		this.having = having;
		return this;
	}

	// TODO
	public IQueryBuilder setLockOptions(LockOptions lockOptions) {
		this.lockOptions = lockOptions;
		return this;
	}

	// ---------------------- PUBLIC EXECUTE-QUERY METHODS

	public <T> List<T> list() {
		buildQuery();
		applyParams();
		if (lockOptions != null) {
			query.setLockOptions(lockOptions);
		}
		return query.list();
	}

	public <T> List<T> list(long firstResult, long maxResults) {
		buildQuery();
		applyParams();

		if (lockOptions != null) {
			query.setLockOptions(lockOptions);
		}

		List<T> list = query
			.setFirstResult((int) firstResult)
			.setMaxResults((int) maxResults)
			.list();

		return list;
	}

	public <T> T object() {
		buildQuery();
		applyParams();
		if (lockOptions != null) {
			query.setLockOptions(lockOptions);
		}
		return (T) query.uniqueResult();
	}

	public int update() {
		Session session = persistorService.getCurrentSession();
		persistorService.checkTransaction(session);

		buildQuery();
		applyParams();

		if (lockOptions != null) {
			query.setLockOptions(lockOptions);
		}

		int result = query.executeUpdate();
		session.flush();
		return result;
	}


	// -------------------------- PRIVATE METHODS

	private void buildQuery() {
		if (query == null) {
			Session session = persistorService.getCurrentSession();
			query = sqlMode ?
				session.createSQLQuery(buildQueryString(true)) :
				session.createQuery(buildQueryString(true));
		}

		/*if (comment != null) {
			query.addQueryHint(String.format("h[%s]", comment));
		}

		for (String hint : hints) {
			query.addQueryHint(hint);
		}*/
	}

	private String buildQueryString(boolean applyOrder) {
		StringBuilder concatAllBuilder = new StringBuilder();
		concatAllBuilder.append(selectBuilder.toString());

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
			for (Map.Entry<String, String> entry : join.entrySet()) {
				concatAllBuilder
					.append(" join ")
					.append(entry.getValue()).append(" ") // join expression
					.append(entry.getKey()).append(" "); // join alias
			}
		}

		if (whereClauseBuilder.length() > 0) {
			concatAllBuilder.append(" where 1=1 ").append(whereClauseBuilder.toString());
		}

		if (groupBy != null) {
			concatAllBuilder.append(" group by ").append(groupBy);
		}

		if (having != null) {
			concatAllBuilder.append(" having ").append(having);
		}

		if (applyOrder && orderBy != null) {
			concatAllBuilder.append(" order by ").append(orderBy);
		}

		String finalQuery = concatAllBuilder.toString();

		if (subQueries.size() > 0) {
			for (Map.Entry<String, IQueryBuilder> subQueryEntry : subQueries.entrySet()) {
				HibernateQueryBuilder builder = (HibernateQueryBuilder) subQueryEntry.getValue(); //TODO
				String query = builder.buildQueryString(false);

				Integer subQueryIndex = subQueriesParamIndex.get(subQueryEntry.getKey());
				for (Map.Entry<String, Object> paramEntry : builder.params.entrySet())
					query = query.replace(":" + paramEntry.getKey(), ":" + paramEntry.getKey() + subQueryIndex);

				finalQuery = finalQuery.replace("#" + subQueryEntry.getKey(), "(" + query + ")");
			}
		}

		logger.debug("HibernateQueryBuilder, final: {}", finalQuery);

		return finalQuery;
	}

	private void applyParams() {
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (param.getValue() instanceof Collection) {
				query.setParameterList(param.getKey(), (Collection) param.getValue());
			} else {
				query.setParameter(param.getKey(), param.getValue());
			}
		}
		if (subQueries.size() > 0) {
			for (Map.Entry<String, IQueryBuilder> entry : subQueries.entrySet()) {
				Integer subQueryIndex = subQueriesParamIndex.get(entry.getKey());

				HibernateQueryBuilder builder = (HibernateQueryBuilder) entry.getValue(); //TODO
				Map<String, Object> params = builder.params;
				for (Map.Entry<String, Object> param : params.entrySet()) {
					if (param.getValue() instanceof Collection)
						query.setParameterList(param.getKey() + subQueryIndex, (Collection) param.getValue());
					else
						query.setParameter(param.getKey() + subQueryIndex, param.getValue());
				}
			}
		}
	}
}
