package org.devocative.demeter.iservice;

import org.devocative.demeter.DemeterException;
import org.devocative.demeter.vo.UserInputVO;

import java.util.List;
import java.util.Map;

public interface IOtherAuthenticationService {
	UserInputVO authenticate(Map<String, List<String>> params) throws DemeterException;
}
