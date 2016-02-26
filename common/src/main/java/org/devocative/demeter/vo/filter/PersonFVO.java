package org.devocative.demeter.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.iservice.persistor.FilterOption;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;

@Filterer
public class PersonFVO implements Serializable {
	private String firstName;

	@FilterOption(useLike = false)
	private String lastName;

	private RangeVO<Date> birthRegDate;

	private Boolean hasUser;

	@FilterOption(property = "birthRegDate")
	private RangeVO<Date> myDate;

	private String sillyProp;


	public String getFirstName() {
		return firstName;
	}

	public PersonFVO setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public PersonFVO setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public RangeVO<Date> getBirthRegDate() {
		return birthRegDate;
	}

	public PersonFVO setBirthRegDate(RangeVO<Date> birthRegDate) {
		this.birthRegDate = birthRegDate;
		return this;
	}

	public Boolean getHasUser() {
		return hasUser;
	}

	public PersonFVO setHasUser(Boolean hasUser) {
		this.hasUser = hasUser;
		return this;
	}

	public RangeVO<Date> getMyDate() {
		return myDate;
	}

	public PersonFVO setMyDate(RangeVO<Date> myDate) {
		this.myDate = myDate;
		return this;
	}

	public String getSillyProp() {
		return sillyProp;
	}

	public PersonFVO setSillyProp(String sillyProp) {
		this.sillyProp = sillyProp;
		return this;
	}
}
