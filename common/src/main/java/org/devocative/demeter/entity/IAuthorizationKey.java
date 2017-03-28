package org.devocative.demeter.entity;

import java.io.Serializable;

public interface IAuthorizationKey extends Serializable {
	String getName();

	void setModule(String module);
}
