package org.devocative.demeter;

import org.devocative.adroit.IConfigKey;

public enum DemeterConfigKey implements IConfigKey {
	Modules("dmt.modules"),
	EnabledSecurity("dmt.security.enabled", true),
	ServiceRemoteHost("dmt.service.remote.host"),
	WebRequestTimeout("dmt.web.request.timeout", 10),
	WebIgnoreMissedResource("dmt.web.ignore.missed.resource", false),
	AuthenticationMode("dmt.authentication.mode", "database"),
	LdapDnTemplate(true, "dmt.ldap.dn.template"),
	LdapUrl(true, "dmt.ldap.url"),
	LdapAttrFirstName("dmt.ldap.attr.firstname"),
	LdapAttrLastName("dmt.ldap.attr.lastname"),

	TaskEnabled("dmt.task.enabled", true),
	TaskPoolSize("dmt.task.pool.size", 5),
	TaskPoolMax("dmt.task.pool.max", 10),
	TaskPoolAliveTime("dmt.task.pool.alive.time", 0),;


	private String key;
	private boolean validate = false;
	private Object defaultValue;

	DemeterConfigKey(String key) {
		this.key = key;
	}

	DemeterConfigKey(boolean validate, String key) {
		this.key = key;
		this.validate = validate;
	}

	DemeterConfigKey(String key, Object defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public boolean getValidate() {
		return validate;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}
}
