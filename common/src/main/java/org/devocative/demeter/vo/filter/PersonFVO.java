package org.devocative.demeter.vo.filter;

import org.devocative.adroit.vo.RangeVO;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.persistor.Filterer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Filterer
public class PersonFVO implements Serializable {
	private static final long serialVersionUID = 3382486365153012902L;

	private String firstName;
	private String lastName;
	private RangeVO<Date> birthRegDate;
	private String email;
	private String mobile;
	private String systemNumber;
	private Boolean hasUser;
	private RangeVO<Date> creationDate;
	private List<User> creatorUser;
	private RangeVO<Date> modificationDate;
	private List<User> modifierUser;
	private RangeVO<Integer> version;

	// ------------------------------

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public RangeVO<Date> getBirthRegDate() {
		return birthRegDate;
	}

	public void setBirthRegDate(RangeVO<Date> birthRegDate) {
		this.birthRegDate = birthRegDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSystemNumber() {
		return systemNumber;
	}

	public void setSystemNumber(String systemNumber) {
		this.systemNumber = systemNumber;
	}

	public Boolean getHasUser() {
		return hasUser;
	}

	public void setHasUser(Boolean hasUser) {
		this.hasUser = hasUser;
	}

	public RangeVO<Date> getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(RangeVO<Date> creationDate) {
		this.creationDate = creationDate;
	}

	public List<User> getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(List<User> creatorUser) {
		this.creatorUser = creatorUser;
	}

	public RangeVO<Date> getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(RangeVO<Date> modificationDate) {
		this.modificationDate = modificationDate;
	}

	public List<User> getModifierUser() {
		return modifierUser;
	}

	public void setModifierUser(List<User> modifierUser) {
		this.modifierUser = modifierUser;
	}

	public RangeVO<Integer> getVersion() {
		return version;
	}

	public void setVersion(RangeVO<Integer> version) {
		this.version = version;
	}
}