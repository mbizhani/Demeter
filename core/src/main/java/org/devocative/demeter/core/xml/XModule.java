package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

@XStreamAlias("module")
public class XModule {

	@XStreamAsAttribute
	private String shortName;

	@XStreamAsAttribute
	private String mainClass;

	@XStreamAsAttribute
	private String mainResource;

	@XStreamAsAttribute
	private String constraintsClass;

	@XStreamAsAttribute
	private String errorCodesClass;

	@XStreamAsAttribute
	private boolean localPersistorService;

	private List<String> dependencies;

	private List<XEntity> entities;

	private List<XDPage> dPages;

	private List<XKey> configKeys;

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getMainResource() {
		return mainResource;
	}

	public void setMainResource(String mainResource) {
		this.mainResource = mainResource;
	}

	public String getConstraintsClass() {
		return constraintsClass;
	}

	public void setConstraintsClass(String constraintsClass) {
		this.constraintsClass = constraintsClass;
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

	public List<XKey> getConfigKeys() {
		return configKeys;
	}

	public void setConfigKeys(List<XKey> configKeys) {
		this.configKeys = configKeys;
	}
}
