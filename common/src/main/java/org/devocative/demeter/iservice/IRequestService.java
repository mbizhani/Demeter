package org.devocative.demeter.iservice;

import org.devocative.demeter.vo.RequestVO;

public interface IRequestService {
	RequestVO getCurrentRequest();

	void set(RequestVO requestVO);

	void unset();
}
