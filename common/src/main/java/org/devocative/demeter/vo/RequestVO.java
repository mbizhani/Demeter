package org.devocative.demeter.vo;

import java.util.List;
import java.util.Map;

public class RequestVO {
	private Map<String, List<String>> params;

	public RequestVO(Map<String, List<String>> params) {
		this.params = params;
	}

	public Map<String, List<String>> getParams() {
		return params;
	}
}
