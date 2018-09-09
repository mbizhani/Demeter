package org.devocative.demeter.service.hibernate;

import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.filter.CollectionUtil;
import org.devocative.demeter.filter.IFilterEvent;
import org.devocative.demeter.iservice.persistor.EJoinMode;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.*;

public class HibernateQueryBuilder implements IQueryBuilder {
	private static final Logger logger = LoggerFactory.getLogger(HibernateQueryBuilder.class);

	private final StringBuilder selectBuilder = new StringBuilder();
	private final StringBuilder whereClauseBuilder = new StringBuilder();
	private final Map<String, Object> params = new HashMap<>();
	private final Map<String, IQueryBuilder> subQueries = new HashMap<>();
	private final Map<String, Integer> subQueriesParamIndex = new HashMap<>();
	private final Map<String, String> join = new LinkedHashMap<>(); // alias -> join expression
	private final Map<String, String> from = new HashMap<>(); // alias -> entity name

	private int subQueriesIndex = 1;
	private String orderBy;
	private String groupBy;
	private String having;

	private boolean sqlMode = false;
	private Query query;
	private LockOptions lockOptions;

	private final HibernatePersistorService persistorService;

	// ------------------------------

	HibernateQueryBuilder(HibernatePersistorService persistorService) {
		this.persistorService = persistorService;
	}

	// ------------------------------

	@Override
	public IQueryBuilder setSqlMode(boolean sqlMode) {
		this.sqlMode = sqlMode;
		return this;
	}

	@Override
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

	@Override
	public IQueryBuilder addParam(String name, Object value) {
		params.put(name, value);
		return this;
	}

	@Override
	public IQueryBuilder addParam(String name, Calendar value) {
		java.sql.Date sqlDate = new java.sql.Date(value.getTimeInMillis());
		params.put(name, sqlDate);
		return this;
	}

	@Override
	public IQueryBuilder addParam(String name, Date value) {
		java.sql.Date sqlDate = new java.sql.Date(value.getTime());
		params.put(name, sqlDate);
		return this;
	}

	@Override
	public IQueryBuilder addParams(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}

	@Override
	public IQueryBuilder addSelect(String selectClause) {
		selectBuilder.append(" ").append(selectClause);
		return this;
	}

	@Override
	public IQueryBuilder addWhere(String whereClause) {
		whereClauseBuilder.append(" ").append(whereClause);
		return this;
	}

	@Override
	public IQueryBuilder addWhere(String whereClause, String paramName, Object paramValue) {
		return addWhere(whereClause).addParam(paramName, paramValue);
	}

	@Override
	public IQueryBuilder addJoin(String alias, String joinExpr) {
		return addJoin(alias, joinExpr, EJoinMode.Inner);
	}

	@Override
	public IQueryBuilder addJoin(String alias, String joinExpr, EJoinMode joinMode) {
		if (!join.containsKey(alias)) {
			if (joinExpr.toLowerCase().contains("join")) {
				join.put(alias, joinExpr);
			} else {
				join.put(alias, String.format("%s %s", joinMode.getExpr(), joinExpr));
			}
		} else {
			throw new DSystemException("Duplicate join alias: " + alias);
		}
		return this;
	}

	@Override
	public IQueryBuilder addSubQueries(String name, IQueryBuilder builder) {
		subQueries.put(name, builder);
		subQueriesParamIndex.put(name, subQueriesIndex++);
		return this;
	}

	@Override
	public IQueryBuilder setOrderBy(String order) {
		this.orderBy = order;
		return this;
	}

	@Override
	public IQueryBuilder setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		return this;
	}

	@Override
	public IQueryBuilder setHaving(String having) {
		this.having = having;
		return this;
	}

	// TODO
	public IQueryBuilder setLockOptions(LockOptions lockOptions) {
		this.lockOptions = lockOptions;
		return this;
	}

	// ---------------

	@Override
	public <T> List<T> list() {
		buildQuery();
		applyParams();
		if (lockOptions != null) {
			query.setLockOptions(lockOptions);
		}
		return query.list();
	}

	@Override
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

	@Override
	public <T> T object() {
		buildQuery();
		applyParams();
		if (lockOptions != null) {
			query.setLockOptions(lockOptions);
		}
		return (T) query.uniqueResult();
	}

	@Override
	public int update() {
		int result = -1;

		persistorService.startTrx();

		try {
			final Session session = persistorService.getCurrentSession();

			buildQuery();
			applyParams();

			if (lockOptions != null) {
				query.setLockOptions(lockOptions);
			}

			result = query.executeUpdate();
			session.flush();

			persistorService.commitOrRollback();
		} catch (PersistenceException e) {
			persistorService.rollback();
			persistorService.processPersistenceException(e);
		} catch (Exception e) {
			persistorService.rollback();
		}

		return result;
	}

	// ---------------

	@Override
	public IQueryBuilder applyFilter(final Class entity, final String alias, final Serializable filter, String... ignoreProperties) {
		CollectionUtil.filter(new IFilterEvent() {
			@Override
			public void ifString(String propName, String value, boolean useLike, boolean caseInsensitive) {
				if (useLike && caseInsensitive) {
					addWhere(String.format("and lower(%1$s.%2$s) like lower(:%2$s_)", alias, propName));
					addParam(propName + "_", "%" + value + "%");
				} else if (useLike) {
					addWhere(String.format("and %1$s.%2$s like :%2$s_", alias, propName));
					addParam(propName + "_", "%" + value + "%");
				} else if (caseInsensitive) {
					addWhere(String.format("and %1$s.%2$s like :%2$s_", alias, propName));
					addParam(propName + "_", value);
				} else {
					addWhere(String.format("and %1$s.%2$s = :%2$s_", alias, propName));
					addParam(propName + "_", value);
				}
			}

			@Override
			public void ifRange(String propName, RangeVO rangeVO) {
				if (rangeVO.getLower() != null) {
					addWhere(String.format("and %1$s.%2$s >= :%2$s_from", alias, propName));
					addParam(propName + "_from", rangeVO.getLower());
				}
				if (rangeVO.getUpper() != null) {
					addWhere(String.format("and %1$s.%2$s < :%2$s_to", alias, propName));
					addParam(propName + "_to", rangeVO.getUpper());
				}
			}

			@Override
			public void ifFilterer(String propName, Object value) {
				Class entityPropertyType;
				String newAlias = alias + "_" + propName;
				addJoin(newAlias, String.format("%s.%s", alias, propName));
				PropertyDescriptor propertyDescriptor = ObjectUtil.getPropertyDescriptor(entity, propName, false);
				if (propertyDescriptor == null) {
					throw new RuntimeException(String.format("Invalid property [%s] in entity [%s]! The filter and entity should have same property name!",
						propName, entity.getName()));
				}
				if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
					entityPropertyType = (Class) propertyDescriptor.getReadMethod().getGenericParameterTypes()[0];
				} else {
					entityPropertyType = propertyDescriptor.getPropertyType();
				}
				applyFilter(entityPropertyType, newAlias, (Serializable) value);
			}

			@Override
			public void ifCollection(String propName, Collection col) {
				if (col.size() > 0) {
					// Check the entity side if it is a collection, which implies that the association is
					// one2many or many2many and it needs a join
					PropertyDescriptor propertyDescriptor = ObjectUtil.getPropertyDescriptor(entity, propName, false);
					if (propertyDescriptor == null) {
						throw new RuntimeException(String.format("Invalid property [%s] in entity [%s]! The filter and entity should have same property name!",
							propName, entity.getName()));
					}
					if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
						String newAlias = alias + "_" + propName;
						addJoin(newAlias, String.format("%s.%s", alias, propName));
						addWhere(String.format("and %1$s in :p_%1$s", newAlias));
						addParam("p_" + newAlias, col);
					} else {
						addWhere(String.format("and %1$s.%2$s in :%2$s_", alias, propName));
						addParam(propName + "_", col);
					}
				}
			}

			@Override
			public void ifOther(String propName, Object value) {
				addWhere(String.format("and %1$s.%2$s = :%2$s_", alias, propName));
				addParam(propName + "_", value);
			}
		}, filter, ignoreProperties);
		return this;
	}

	// ------------------------------

	private void buildQuery() {
		if (query == null) {
			Session session = persistorService.getCurrentSession();
			query = sqlMode ?
				session.createNativeQuery(buildQueryString(true)) :
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
					.append(" ")
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
