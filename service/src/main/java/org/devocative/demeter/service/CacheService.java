package org.devocative.demeter.service;

import org.devocative.adroit.cache.ICache;
import org.devocative.adroit.cache.IMissedHitHandler;
import org.devocative.adroit.cache.LRUCache;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.CacheInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("dmtCacheService")
public class CacheService implements ICacheService {
	private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

	private final Map<String, ICache<?, ?>> ALL_CACHES = new LinkedHashMap<>();

	@Autowired
	private ISecurityService securityService;

	// ------------------------------

	@Override
	public <K, V> ICache<K, V> create(String id, int capacity) {
		return create(id, capacity, null);
	}

	public <K, V> ICache<K, V> create(String id, int capacity, IMissedHitHandler<K, V> missedHitHandler) {
		logger.info("ICache created: {}, capacity = {}", id, capacity);
		ICache<K, V> cache = new LRUCache<>(capacity, missedHitHandler);
		ALL_CACHES.put(id, cache);
		return cache;
	}

	@Override
	public void clear(String id) {
		if (ALL_CACHES.containsKey(id)) {
			logger.warn("ICache cleared: ID=[{}] User=[{}]", id, securityService.getCurrentUser());
			ALL_CACHES.get(id).clear();
		} else {
			logger.warn("Try to clear cache which has not been created: {}", id);
		}
	}

	@Override
	public List<CacheInfoVO> list() {
		List<CacheInfoVO> result = new ArrayList<>();
		for (Map.Entry<String, ICache<?, ?>> entry : ALL_CACHES.entrySet()) {
			CacheInfoVO vo = new CacheInfoVO()
				.setId(entry.getKey())
				.setCapacity(entry.getValue().getCapacity())
				.setSize(entry.getValue().getSize())
				.setMissHitCount(entry.getValue().getMissHitCount());
			result.add(vo);
		}
		return result;
	}
}
