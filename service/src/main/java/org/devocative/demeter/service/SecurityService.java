package org.devocative.demeter.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.entity.EAuthMechanism;
import org.devocative.demeter.entity.ERowMod;
import org.devocative.demeter.entity.EUserStatus;
import org.devocative.demeter.entity.User;
import org.devocative.demeter.iservice.*;
import org.devocative.demeter.vo.UserInputVO;
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
import java.util.*;

@Service("dmtSecurityService")
public class SecurityService implements ISecurityService, IApplicationLifecycle, IRequestLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	private static ThreadLocal<UserVO> CURRENT_USER = new ThreadLocal<>();

	private UserVO system, guest;

	@Autowired
	private IUserService userService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IPageService pageService;

	@Autowired(required = false)
	private IOtherAuthenticationService otherAuthenticationService;

	// ------------------------------ IApplicationLifecycle METHODS

	@Override
	public void init() {
		system = userService.createOrUpdateUser(
			new UserInputVO("system", null, "", "system", EAuthMechanism.DATABASE)
				.setStatus(EUserStatus.DISABLED)
		);
		authenticate(system);

		userService.createOrUpdateUser(
			new UserInputVO("root", "root", "", "root", EAuthMechanism.DATABASE)
				.setAdmin(true)
		);

		guest = userService.createOrUpdateUser(
			new UserInputVO("guest", null, "", "guest", EAuthMechanism.DATABASE)
				.setStatus(EUserStatus.DISABLED)
		);

		if (!ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity)) {
			guest.setAuthenticated(true);
			guest.setDefaultPages(pageService.getDefaultPages());
		}

		roleService.createOrUpdateRole("AuthByDB", ERowMod.SYSTEM);
		roleService.createOrUpdateRole("AuthByLDAP", ERowMod.SYSTEM);
		roleService.createOrUpdateRole("AuthByOther", ERowMod.SYSTEM);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Medium;
	}

	// -------------------------- IRequestLifecycle implementation

	@Override
	public void beforeRequest() {
	}

	@Override
	public void afterResponse() {
		CURRENT_USER.remove();
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
		UserVO authenticatedUserVO;
		User user = userService.loadByUsername(username);

		if (user != null) {
			verifyUser(user);

			if (EAuthMechanism.DATABASE.equals(user.getAuthMechanism())) {
				authenticatedUserVO = authenticateByDatabase(user, password);
			} else if (EAuthMechanism.LDAP.equals(user.getAuthMechanism())) {
				authenticatedUserVO = authenticateByLDAP(username, password);
			} else if (EAuthMechanism.OTHER.equals(user.getAuthMechanism())) {
				if (otherAuthenticationService != null) {
					authenticatedUserVO = authenticateByOther(username, password);
				} else {
					throw new DSystemException("No IOtherAuthenticationService bean defined: user = " + username);
				}
			} else {
				throw new DSystemException("Unknown authenticate mechanism: user = " + username);
			}
		} else {
			String mode = ConfigUtil.getString(DemeterConfigKey.AuthenticationMode);

			if ("database".equalsIgnoreCase(mode)) {
				throw new DemeterException(DemeterErrorCode.InvalidUser);
			} else if ("ldap".equalsIgnoreCase(mode)) {
				authenticatedUserVO = authenticateByLDAP(username, password);
			} else if ("other".equalsIgnoreCase(mode)) {
				if (otherAuthenticationService != null) {
					authenticatedUserVO = authenticateByOther(username, password);
				} else {
					throw new DSystemException("No IOtherAuthenticationService bean defined, but mode is 'other'!");
				}
			} else {
				throw new DSystemException("Unknown authenticate mode: " + mode);
			}
		}

		afterAuthentication(authenticatedUserVO);
	}

	@Override
	public void authenticate(Map<String, List<String>> params) {
		/*if (params.containsKey(USERNAME_KEY) && params.containsKey(PASSWORD_KEY)) {
			authenticate(params.get(USERNAME_KEY).get(0), params.get(PASSWORD_KEY).get(0));
		} else */
		if (otherAuthenticationService != null) {
			UserVO authenticatedUserVO = otherAuthenticationService.authenticate(params);
			if (authenticatedUserVO != null) {
				afterAuthentication(authenticatedUserVO);
			}
		}
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

	private UserVO authenticateByDatabase(User user, String password) {
		if (password != null) {
			password = StringEncryptorUtil.hash(password);
			if (password.equals(user.getPassword())) {
				return userService.getUserVO(user);
			} else {
				throw new DemeterException(DemeterErrorCode.InvalidUser);
			}
		} else {
			throw new DemeterException(DemeterErrorCode.InvalidUser);
		}
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

			return userService.createOrUpdateUser(new UserInputVO(username, password, firstName, lastName, EAuthMechanism.LDAP));
		} catch (AuthenticationException e) {
			logger.warn("authenticateByLDAP failed for user: {}", username);
			throw new DemeterException(DemeterErrorCode.InvalidUser, username);
		} catch (NamingException e) {
			logger.error("AUTHENTICATE BY LDAP ERROR: ", e);
			throw new DSystemException("LDAP Server Problem: ", e);
		}
	}

	private UserVO authenticateByOther(String username, String password) {
		Map<String, List<String>> params = new HashMap<>();
		params.put(USERNAME_KEY, Collections.singletonList(username));
		params.put(PASSWORD_KEY, Collections.singletonList(password));
		UserVO userVO = otherAuthenticationService.authenticate(params);
		if (userVO == null) {
			throw new DemeterException(DemeterErrorCode.InvalidUser);
		}
		return userService.createOrUpdateUser(new UserInputVO(username, password, userVO.getFirstName(), userVO.getLastName(), EAuthMechanism.OTHER));
	}

	private void afterAuthentication(UserVO authenticatedUserVO) {
		authenticatedUserVO.setAuthenticated(true);
		CURRENT_USER.set(authenticatedUserVO);
	}

	private void verifyUser(User user) {
		if (EUserStatus.DISABLED.equals(user.getStatus())) {
			throw new DemeterException(DemeterErrorCode.UserDisabled);
		} else if (EUserStatus.LOCKED.equals(user.getStatus())) {
			throw new DemeterException(DemeterErrorCode.UserLocked);
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
