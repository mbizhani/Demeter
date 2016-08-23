package org.devocative.demeter.vo;

import java.io.Serializable;

public class CacheInfoVO implements Serializable {
	private static final long serialVersionUID = 8862349754728879777L;

	private String id;
	private int capacity;
	private int size;
	private long missHitCount;

	// ------------------------------

	public String getId() {
		return id;
	}

	public CacheInfoVO setId(String id) {
		this.id = id;
		return this;
	}

	public int getCapacity() {
		return capacity;
	}

	public CacheInfoVO setCapacity(int capacity) {
		this.capacity = capacity;
		return this;
	}

	public int getSize() {
		return size;
	}

	public CacheInfoVO setSize(int size) {
		this.size = size;
		return this;
	}

	public long getMissHitCount() {
		return missHitCount;
	}

	public CacheInfoVO setMissHitCount(long missHitCount) {
		this.missHitCount = missHitCount;
		return this;
	}
}
