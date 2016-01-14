package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.Date;

public interface IModificationDate extends Serializable {
	Date getModificationDate();

	void setModificationDate(Date date);

	Integer getVersion();

	void setVersion(Integer version);
}
