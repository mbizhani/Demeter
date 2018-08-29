package org.devocative.demeter.service;

import org.devocative.demeter.iservice.IRequestService;
import org.devocative.demeter.vo.RequestVO;
import org.springframework.stereotype.Service;

@Service("dmtRequestService")
public class RequestService implements IRequestService {
	private static final ThreadLocal<RequestVO> CURRENT_REQUEST = new ThreadLocal<>();

	// ------------------------------

	@Override
	public RequestVO getCurrentRequest() {
		return CURRENT_REQUEST.get();
	}

	@Override
	public void set(RequestVO requestVO) {
		CURRENT_REQUEST.set(requestVO);
	}

	@Override
	public void unset() {
		CURRENT_REQUEST.remove();
	}
}
