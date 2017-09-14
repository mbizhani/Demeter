package org.devocative.demeter;

import org.devocative.adroit.IConfigKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DemeterConfigKey implements IConfigKey {
	Modules("dmt.modules"),
	DeploymentMode("dmt.deployment.enabled", true, Arrays.asList(true, false)),

	EnabledSecurity("dmt.security.enabled", true, Arrays.asList(true, false)),
	SecurityRealm(true, "dmt.security.realm"),
	SecurityKeyStoreEnabled("dmt.security.ks.enabled", true, Arrays.asList(true, false)),
	SecurityKeyStoreToken("dmt.security.ks.token", "DEMETER_TOKEN"),
	SecurityKeyStoreParam("dmt.security.ks.param", "DEMETER_PARAM"),
	SecurityKeyStoreEntry("dmt.security.ks.entry", "Demeter"),
	AuthenticationMode("dmt.security.auth.mode", (Object) Collections.emptyList()),
	HttpAuthenticationMode("dmt.security.http.mode", "basic", Arrays.asList("basic", "digest")),
	HttpPort("dmt.security.http.port", 8080),
	HttpsEnabled("dmt.security.https.enabled", false, Arrays.asList(true, false)),
	HttpsPort("dmt.security.https.port", 8443),
	UserAutoRegister("dmt.security.register.auto", true, Arrays.asList(true, false)),
	LoginCaptchaEnabled("dmt.security.login.captcha.enabled", true, Arrays.asList(true, false)),
	UrlCrypticEnabled("dmt.security.url.cryptic.enabled", false, Arrays.asList(true, false)),
	CsrfPreventionEnabled("dmt.security.csrf.prevention.enabled", false, Arrays.asList(true, false)),
	HttpAuthFilterSkip("dmt.security.filter.skip", false, Arrays.asList(true, false)),

	CorsEnabled("dmt.security.cors.enabled", false, Arrays.asList(true, false)),
	CorsHeaderOrigins("dmt.security.cors.origins", "*"),
	CorsHeaderHeaders("dmt.security.cors.headers", "Origin, X-Requested-With, Content-Type, Accept"),
	CorsHeaderMethods("dmt.security.cors.methods", "GET, POST, HEAD, OPTIONS"),
	CorsHeaderCredentials("dmt.security.cors.credential", "true"),

	ServiceRemoteHost("dmt.service.remote.host"),
	WebRequestTimeout("dmt.web.request.timeout", 10),
	WebIgnoreMissedResource("dmt.web.ignore.missed.resource", false, Arrays.asList(true, false)),
	PingWebSocketEnabled("dmt.server.ping.enabled", true, Arrays.asList(true, false)),
	PingWebSocketPeriod("dmt.server.ping.period", 150000), //4.5 * 60 * 1000

	LdapDnTemplate(true, "dmt.ldap.dn.template"),
	LdapUrl(true, "dmt.ldap.url"),
	LdapAttrFirstName("dmt.ldap.attr.firstname"),
	LdapAttrLastName("dmt.ldap.attr.lastname"),

	TaskEnabled("dmt.task.enabled", true, Arrays.asList(true, false)),
	TaskPoolSize("dmt.task.pool.size", 5),
	TaskPoolMax("dmt.task.pool.max", 10),
	TaskPoolAliveTime("dmt.task.pool.alive.time", 0),

	StringTemplateCacheEnabled("dmt.string.template.cache.enabled", true, Arrays.asList(true, false)),

	FileBaseDir(true, "dmt.file.base.dir"),

	UserDefaultSessionTimeout("dmt.user.def.session.timeout", 60),
	UserDefaultLocale("dmt.user.def.locale", "fa", Arrays.asList("fa", "en")),
	UserDefaultCalendar("dmt.user.def.calendar", "Jalali", Arrays.asList("Jalali", "Gregorian")),
	UserDefaultLayout("dmt.user.def.layout", "RTL", Arrays.asList("RTL", "LTR")),

	LogMDCEnabled("dmt.log.mdc.enabled", true, Arrays.asList(true, false));


	private String key;
	private boolean validate = false;
	private Object defaultValue;
	private List<?> possibilities;

	DemeterConfigKey(String key) {
		this(false, key, null);
	}

	DemeterConfigKey(String key, List<?> possibilities) {
		this(false, key, possibilities);
	}

	DemeterConfigKey(boolean validate, String key) {
		this(validate, key, null);
	}

	// Main Constructor 1
	DemeterConfigKey(boolean validate, String key, List<?> possibilities) {
		this.key = key;
		this.validate = validate;
		this.possibilities = possibilities;
	}

	DemeterConfigKey(String key, Object defaultValue) {
		this(key, defaultValue, null);
	}

	// Main Constructor 2
	DemeterConfigKey(String key, Object defaultValue, List<?> possibilities) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.possibilities = possibilities;
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

	@Override
	public List<?> getPossibleValues() {
		return possibilities;
	}
}
