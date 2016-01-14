package org.devocative.demeter.entity;

import java.io.Serializable;

public interface IModifierUser extends Serializable {
	Long getModifierUserId();

	void setModifierUserId(Long userId);
}
