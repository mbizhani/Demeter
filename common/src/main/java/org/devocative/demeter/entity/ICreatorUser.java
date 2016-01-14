package org.devocative.demeter.entity;

import java.io.Serializable;

public interface ICreatorUser extends Serializable {
	Long getCreatorUserId();

	void setCreatorUserId(Long userId);
}
