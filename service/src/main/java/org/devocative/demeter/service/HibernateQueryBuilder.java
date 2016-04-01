package org.devocative.demeter.service;

import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.iservice.persistor.FilterOption;
import org.devocative.demeter.iservice.persistor.Filterer;
import org.devocative.demeter.iservice.persistor.IQueryBuilder;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

	//----------------------------- PRIVATE METHODS - Search Builder

	@Override
	public IQueryBuilder applyFilter(Class entity, String alias, Serializable filter, String... ignoreProperties) {
		PropertyDescriptor[] descriptors = ObjectUtil.getPropertyDescriptors(filter, false);

		List<String> ignorePropsList = new ArrayList<>();
		ignorePropsList.add("class");
		Collections.addAll(ignorePropsList, ignoreProperties);

		for (PropertyDescriptor descriptor : descriptors) {
			String propName = descriptor.getName();
			Method readMethod = descriptor.getReadMethod();
			FilterOption search = findAnnotation(FilterOption.class, filter, descriptor);
			if (search != null && search.property().length() > 0) {
				propName = search.property();
			}

			if (ignorePropsList.contains(propName)) {
				continue;
			}

			try {
				Object value = readMethod.invoke(filter);

				if (value == null) {
					continue;
				}

				// ---------- Property: String
				if (value instanceof String) {
					if (search != null && !search.useLike()) {
						addWhere(String.format("and %1$s.%2$s = :%2$s_", alias, propName));
						addParam(propName + "_", value);
					} else {
						addWhere(String.format("and %1$s.%2$s like :%2$s_", alias, propName));
						addParam(propName + "_", "%" + value + "%");
					}
				}

				// ---------- Property: RangeVO
				else if (value instanceof RangeVO) {
					RangeVO rangeVO = (RangeVO) value;
					if (rangeVO.getLower() != null) {
						addWhere(String.format("and %1$s.%2$s >= :%2$s_from", alias, propName));
						addParam(propName + "_from", rangeVO.getLower());
					}
					if (rangeVO.getUpper() != null) {
						addWhere(String.format("and %1$s.%2$s < :%2$s_to", alias, propName));
						addParam(propName + "_to", rangeVO.getUpper());
					}
				}

				// ---------- Property: an object of Filterer
				else if (value.getClass().isAnnotationPresent(Filterer.class)) {
					Class entityPropertyType;
					String newAlias = alias + "_" + propName;
					addJoin(newAlias, String.format("join %s.%s %s", alias, propName, newAlias));
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

				// ---------- Property: Collection
				else if (value instanceof Collection) {
					Collection col = (Collection) value;
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
							addJoin(newAlias, String.format("join %s.%s %s", alias, propName, newAlias));
							addWhere(String.format("and %1$s in :p_%1$s", newAlias));
							addParam("p_" + newAlias, value);
						} else {
							addWhere(String.format("and %1$s.%2$s in :%2$s_", alias, propName));
							addParam(propName + "_", value);
						}
					}
				}
				// ---------- Property: other primitive types
				else {
					addWhere(String.format("and %1$s.%2$s = :%2$s_", alias, propName));
					addParam(propName + "_", value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return this;
	}

	private <T extends Annotation> T findAnnotation(Class<? extends Annotation> annot, Object obj,
													PropertyDescriptor propertyDescriptor) {
		T result = null;
		Method readMethod = propertyDescriptor.getReadMethod();
		if (readMethod != null)
			result = (T) readMethod.getAnnotation(annot);

		if (result == null) {
			try {
				Field field = obj.getClass().getDeclaredField(propertyDescriptor.getName());
				result = (T) field.getAnnotation(annot);
			} catch (NoSuchFieldException e) {
			}
		}
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
