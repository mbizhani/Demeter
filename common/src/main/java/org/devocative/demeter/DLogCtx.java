package org.devocative.demeter;

import org.apache.log4j.MDC;
import org.devocative.adroit.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DLogCtx {
	private static final Logger logger = LoggerFactory.getLogger(DLogCtx.class);
	private static final DLogCtx INST = new DLogCtx();

	// ------------------------------

	public static DLogCtx put(String key, Object val) {
		Boolean enabled = ConfigUtil.getBoolean(DemeterConfigKey.LogMDCEnabled);

		if (enabled && key != null) {
			try {
				if (val != null) {
					MDC.put(key, val);
				} else {
					MDC.remove(key);
				}
			} catch (Exception e) {
				logger.warn("MDC.put: key=[{}] val=[{}]", key, val, e);
			}
		}

		return INST;
	}

	public static DLogCtx putAsList(String key, Object... val) {
		Boolean enabled = ConfigUtil.getBoolean(DemeterConfigKey.LogMDCEnabled);
		if (enabled && key != null && val != null && val.length > 0) {
			List<Object> list = (List<Object>) MDC.get(key);
			if (list == null) {
				list = new ArrayList<>();
				MDC.put(key, list);
			}
			Collections.addAll(list, val);
		}

		return INST;
	}

	public static DLogCtx remove(String key) {
		Boolean enabled = ConfigUtil.getBoolean(DemeterConfigKey.LogMDCEnabled);
		if (enabled) {
			try {
				MDC.remove(key);
			} catch (Exception e) {
				logger.error("DLogCtx.remove: key=[{}]", key, e);
			}
		}
		return INST;
	}

	// ------------------------------

	private DLogCtx() {
	}
}
