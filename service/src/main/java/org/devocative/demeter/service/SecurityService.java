package org.devocative.demeter.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.*;
import org.devocative.demeter.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

@Service("dmtSecurityService")
public class SecurityService implements ISecurityService, IApplicationLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

	private static ThreadLocal<UserVO> CURRENT_USER = new ThreadLocal<>();

	private UserVO system, guest;

	@Autowired
	private IUserService userService;

	@Autowired
	private IPageService pageService;

	// ------------------------------ IApplicationLifecycle METHODS

	@Override
	public void init() {
		system = userService.createOrUpdateUser("system", "system", "system", "system");
		guest = userService.createOrUpdateUser("guest", "guest", "guest", "guest");
		guest.setAuthenticated(!ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity));
		if (guest.isAuthenticated()) {
			guest.setDefaultPages(pageService.getDefaultPages());
		}
		authenticate(system);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Medium;
	}

	// ------------------------------ PUBLIC METHODS

	@Override
	public UserVO getCurrentUser() {
		return CURRENT_USER.get() != null ? CURRENT_USER.get() : guest;
	}

	@Override
	public void authenticate(UserVO userVO) {
		//TODO some checking, and maybe reloading the user's data
		if (userVO != null) {
			CURRENT_USER.set(userVO);
		} else {
			CURRENT_USER.set(guest);
		}
	}

	@Override
	public void authenticate(String username, String password) {
		String mode = ConfigUtil.getString(DemeterConfigKey.AuthenticationMode);
		logger.debug("Authenticate Mode: {}", mode);

		UserVO userVO;
		if ("database".equalsIgnoreCase(mode)) {
			userVO = authenticateByDatabase(username, password);
		} else if ("ldap".equalsIgnoreCase(mode)) {
			userVO = authenticateByLDAP(username, password);
		} else {
			throw new DSystemException("Unknown authentication mode: " + mode);
		}
		userVO.setAuthenticated(true);
		//TODO find authorized dPages
		userVO.setDefaultPages(pageService.getDefaultPages());
		CURRENT_USER.set(userVO);
	}

	@Override
	public void signOut() {
		CURRENT_USER.set(guest);
	}

	@Override
	public String getUserDigest(String username) {
		//TODO the password must be saved symmetric-encoded or the following hash must be persisted somewhere
		User user = userService.loadByUsername(username);

		if (user != null) {
			return DigestUtils.md5Hex(
				user.getUsername() + ":" +
					ConfigUtil.getString(DemeterConfigKey.SecurityRealm) + ":" +
					user.getPassword());
		}

		return null;
	}

	@Override
	public UserVO getSystemUser() {
		return system;
	}

	// ------------------------------ PRIVATE METHODS

	private UserVO authenticateByDatabase(String username, String password) {
		// TODO implement this!
		throw new DSystemException("authenticateByDatabase not implemented");
	}

	private UserVO authenticateByLDAP(String username, String password) {
		String dnTemplate = ConfigUtil.getString(DemeterConfigKey.LdapDnTemplate);
		String dn = String.format(dnTemplate, username);

		logger.debug("User DN: {}", dn);

		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ConfigUtil.getString(DemeterConfigKey.LdapUrl));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			DirContext context = new InitialDirContext(env);
			Attributes attrs = context.getAttributes(dn);

			String firstName = null;
			String firstNameAttr = ConfigUtil.getString(DemeterConfigKey.LdapAttrFirstName);
			if (firstNameAttr != null) {
				firstName = getValue(attrs.get(firstNameAttr));
			}

			String lastName = null;
			String lastNameAttr = ConfigUtil.getString(DemeterConfigKey.LdapAttrLastName);
			if (lastNameAttr != null) {
				lastName = getValue(attrs.get(lastNameAttr));
			}

			return userService.createOrUpdateUser(username, password, firstName, lastName);
		} catch (AuthenticationException e) {
			logger.warn("authenticateByLDAP failed for user: {}", username);
			throw new DemeterException(DemeterErrorCode.InvalidUser, username);
		} catch (NamingException e) {
			logger.error("AUTHENTICATE BY LDAP ERROR: ", e);
			throw new DSystemException("LDAP Server Problem:", e);
		}
	}

	private String getValue(Attribute attribute) {
		try {
			if (attribute != null && attribute.get() != null) {
				return attribute.get().toString();
			}
		} catch (NamingException e) {
			logger.error("LDAP getValue for attr = " + attribute, e);
		}
		return null;
	}
}
