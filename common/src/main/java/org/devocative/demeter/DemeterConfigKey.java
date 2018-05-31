package org.devocative.demeter;

import org.devocative.adroit.IConfigKey;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public enum DemeterConfigKey implements IConfigKey {
	Modules("dmt.modules"),
	DeploymentMode("dmt.deployment.enabled", true, Arrays.asList(true, false)),

	DatabaseDiffHandler("dmt.db.diff.handler", "script", Arrays.asList("script", "auto", "hbm2ddl", "none")),
	DatabaseCheckTimeoutEnabled("dmt.db.check.timeout.enabled", true, Arrays.asList(true, false)),
	DatabaseCheckTimeoutDur("dmt.db.check.timeout.dur", 15),
	DatabaseCheckTimeoutMin("dmt.db.check.timeout.min", 5),
	DatabaseCheckTimeoutMax("dmt.db.check.timeout.max", 10),
	DatabaseCheckTimeoutList("dmt.db.check.timeout.list", 10),
	DatabaseCheckTimeoutAlive("dmt.db.check.timeout.alive", 10), //minutes

	EnabledSecurity("dmt.security.enabled", true, Arrays.asList(true, false)),
	//SecurityRealm(true, "dmt.security.realm"),
	SecurityKeyStoreEnabled("dmt.security.ks.enabled", true, Arrays.asList(true, false)),
	SecurityKeyStoreToken("dmt.security.ks.token", "DEMETER_TOKEN"),
	SecurityKeyStoreParam("dmt.security.ks.param", "DEMETER_PARAM"),
	SecurityKeyStoreEntry("dmt.security.ks.entry", "Demeter"),
	//AuthenticationMode("dmt.security.auth.mode", (Object) Collections.emptyList()),
	//HttpAuthenticationMode("dmt.security.http.mode", "basic", Arrays.asList("basic", "digest")),
	HttpPort("dmt.security.http.port", 8080),
	HttpsEnabled("dmt.security.https.enabled", false, Arrays.asList(true, false)),
	HttpsPort("dmt.security.https.port", 8443),
	UserAutoRegister("dmt.security.register.auto", false, Arrays.asList(true, false)),
	LoginCaptchaEnabled("dmt.security.login.captcha.enabled", true, Arrays.asList(true, false)),
	UrlCrypticEnabled("dmt.security.url.cryptic.enabled", false, Arrays.asList(true, false)),
	CsrfPreventionEnabled("dmt.security.csrf.prevention.enabled", false, Arrays.asList(true, false)),
	HttpAuthFilterEnabled("dmt.security.filter.enabled", true, Arrays.asList(true, false)),

	OtherAuthUserPassEnabled("dmt.security.other.userpass.enabled", false, Arrays.asList(true, false)),
	OtherAuthUsernameParam("dmt.security.other.username.param", "username"),
	OtherAuthPasswordParam("dmt.security.other.password.param", "password"),
	OtherAuthUpdate("dmt.security.other.update", false, Arrays.asList(true, false)),

	//STO: Session Time Out
	STO_Database("dmt.security.sto.database", 60),
	STO_LDAP("dmt.security.sto.ldap", 40),
	STO_Other("dmt.security.sto.other", 20),
	STO_Admin("dmt.security.sto.admin", 10),

	CorsEnabled("dmt.security.cors.enabled", false, Arrays.asList(true, false)),
	CorsHeaderOrigins("dmt.security.cors.origins", "*"),
	CorsHeaderHeaders("dmt.security.cors.headers", "Origin, X-Requested-With, Content-Type, Accept"),
	CorsHeaderMethods("dmt.security.cors.methods", "GET, POST, HEAD, OPTIONS"),
	CorsHeaderCredentials("dmt.security.cors.credential", "true"),

	DPageInstRolesByXML("dmt.service.dpage.roles.xml", false, Arrays.asList(true, false)),
	StringTemplateCacheEnabled("dmt.service.string.template.cache.enabled", true, Arrays.asList(true, false)),
	FileBaseDir(true, "dmt.service.file.base.dir"),
	//TODO ServiceRemoteHost("dmt.service.remote.host"),

	WebAppName("dmt.web.app.name"),
	WebRequestTimeout("dmt.web.request.timeout", 10),
	WebIgnoreMissedResource("dmt.web.ignore.missed.resource", false, Arrays.asList(true, false)),
	WebReplaceCharForString("dmt.web.replace.char", "{\"fa\":[{\"from\":\"ي\",\"to\":\"ی\"}, {\"from\":\"ك\",\"to\":\"ک\"}]}"),

	WebPasswordStrength("dmt.web.password.strength", false, Arrays.asList(true, false)),
	WebPasswordStrengthDigit("dmt.web.password.strength.digit", true, Arrays.asList(true, false)),
	WebPasswordStrengthLowerCase("dmt.web.password.strength.lower", true, Arrays.asList(true, false)),
	WebPasswordStrengthUpperCase("dmt.web.password.strength.upper", true, Arrays.asList(true, false)),
	WebPasswordStrengthSpecialChar("dmt.web.password.strength.special", true, Arrays.asList(true, false)),
	WebPasswordStrengthNoWhiteSpace("dmt.web.password.strength.nospace", true, Arrays.asList(true, false)),
	WebPasswordStrengthMinLength("dmt.web.password.strength.minlength", 7),
	WebPasswordStrengthMaxLength("dmt.web.password.strength.maxlength"),
	PingWebSocketEnabled("dmt.web.ws.ping.enabled", true, Arrays.asList(true, false)),
	PingWebSocketPeriod("dmt.web.ws.ping.period", 270), //4.5 * 60

	StartupGroovyScript("dmt.server.startup.script"),

	LdapDnTemplate(true, "dmt.ldap.dn.template"),
	LdapUrl(true, "dmt.ldap.url"),
	LdapAttrFirstName("dmt.ldap.attr.firstname", "givenname"),
	LdapAttrLastName("dmt.ldap.attr.lastname", "sn"),

	TaskEnabled("dmt.task.enabled", true, Arrays.asList(true, false)),
	TaskPoolSize("dmt.task.pool.size", 5),
	TaskPoolMax("dmt.task.pool.max", 10),
	TaskPoolAliveTime("dmt.task.pool.alive.time", 10), //minutes

	UserDefaultLocale("dmt.user.def.locale", "fa", Arrays.asList("fa", "en")),
	UserDefaultCalendar("dmt.user.def.calendar", Arrays.asList("Persian", "Gregorian")),
	UserDefaultTimeZone("dmt.user.def.timezone", TimeZone.getDefault().getID()),

	LogMDCEnabled("dmt.log.mdc.enabled", true, Arrays.asList(true, false));

	// ------------------------------

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
