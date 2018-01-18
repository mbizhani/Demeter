package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

@XStreamAlias("module")
public class XModule {

	@XStreamAsAttribute
	private String shortName;

	@XStreamAsAttribute
	private String mainResource;

	@XStreamAsAttribute
	private String privilegeKeyClass;

	@XStreamAsAttribute
	private String errorCodesClass;

	@XStreamAsAttribute
	private boolean localPersistorService;

	@XStreamAsAttribute
	private String configKeyClass;

	private List<String> dependencies;

	private List<XEntity> entities;

	private List<XDPage> dPages;

	private List<XDTask> tasks;

	// ------------------------------

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getMainResource() {
		return mainResource;
	}

	public void setMainResource(String mainResource) {
		this.mainResource = mainResource;
	}

	public String getPrivilegeKeyClass() {
		return privilegeKeyClass;
	}

	public void setPrivilegeKeyClass(String privilegeKeyClass) {
		this.privilegeKeyClass = privilegeKeyClass;
	}

	public String getErrorCodesClass() {
		return errorCodesClass;
	}

	public void setErrorCodesClass(String errorCodesClass) {
		this.errorCodesClass = errorCodesClass;
	}

	public boolean isLocalPersistorService() {
		return localPersistorService;
	}

	public void setLocalPersistorService(boolean localPersistorService) {
		this.localPersistorService = localPersistorService;
	}

	public String getConfigKeyClass() {
		return configKeyClass;
	}

	public XModule setConfigKeyClass(String configKeyClass) {
		this.configKeyClass = configKeyClass;
		return this;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}

	public List<XEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<XEntity> entities) {
		this.entities = entities;
	}

	public List<XDPage> getDPages() {
		return dPages;
	}

	public XModule setDPages(List<XDPage> dPages) {
		this.dPages = dPages;
		return this;
	}

	public List<XDTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<XDTask> tasks) {
		this.tasks = tasks;
	}
}
