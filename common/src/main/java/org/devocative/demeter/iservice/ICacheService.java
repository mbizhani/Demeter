package org.devocative.demeter.iservice;

import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.demeter.vo.CacheInfoVO;

import java.util.List;

public interface ICacheService {
	<K, V> ICache<K, V> create(String id, int max);

	<K, V> ICache<K, V> create(String id, int max, IMissedHitHandler<K, V> missedHitHandler);

	void clear(String id);

	List<CacheInfoVO> list();
}
