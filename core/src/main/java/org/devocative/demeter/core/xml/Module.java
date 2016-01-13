package org.devocative.demeter.core.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

@XStreamAlias("module")
public class Module {

	@XStreamAsAttribute
	private String shortName;

	@XStreamAsAttribute
	private String mainClass;

	@XStreamAsAttribute
	private String constraintsClass;

	@XStreamAsAttribute
	private String errorCodesClass;

	@XStreamAsAttribute
	private boolean localPersistorService;

	private List<String> dependencies;

	private List<Entity> entities;

	private List<Key> configKeys;

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

	public List<Entity> getEntities() {
		return entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public List<Key> getConfigKeys() {
		return configKeys;
	}

	public void setConfigKeys(List<Key> configKeys) {
		this.configKeys = configKeys;
	}
}
