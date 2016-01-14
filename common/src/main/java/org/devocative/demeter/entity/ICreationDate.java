package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.Date;

public interface ICreationDate extends Serializable {
	Date getCreationDate();

	void setCreationDate(Date date);
}
