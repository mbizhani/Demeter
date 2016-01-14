package org.devocative.demeter.service;

import org.devocative.demeter.iservice.ISecurityService;
import org.springframework.stereotype.Service;

@Service("dmtSecurityService")
public class DemeterSecurityService implements ISecurityService {
	@Override
	public Long getCurrentUserId() {
		return null;
	}
}
