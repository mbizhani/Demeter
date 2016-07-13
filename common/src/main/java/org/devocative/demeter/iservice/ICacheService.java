package org.devocative.demeter.iservice;

import org.devocative.adroit.cache.ICache;
import org.devocative.demeter.vo.CacheInfoVO;

import java.util.List;

public interface ICacheService {
	<V> ICache<String, V> create(String id, int max);

	void clear(String id);

	List<CacheInfoVO> list();
}
