package org.devocative.demeter.iservice.persistor;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IQueryBuilder {

	IQueryBuilder setSqlMode(boolean sqlMode);

	IQueryBuilder addFrom(String entity, String alias);

	IQueryBuilder addFrom(Class entity, String alias);

	IQueryBuilder addParam(String name, Object value);

	IQueryBuilder addParam(String name, Calendar value);

	IQueryBuilder addParam(String name, Date value);

	IQueryBuilder addParams(Map<String, Object> params);

	IQueryBuilder addSelect(String selectClause);

	IQueryBuilder addWhere(String whereClause);

	IQueryBuilder addJoin(String alias, String joinExpr);

	IQueryBuilder addSubQueries(String name, IQueryBuilder builder);

	IQueryBuilder setOrderBy(String order);

	IQueryBuilder setGroupBy(String groupBy);

	IQueryBuilder setHaving(String having);

	<T> List<T> list();

	<T> List<T> list(long firstResult, long maxResults);

	<T> T object();

	int update();
}
