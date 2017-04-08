package org.devocative.demeter.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.DSystemException;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.core.ModuleLoader;
import org.devocative.demeter.core.xml.XModule;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.*;
import org.devocative.demeter.iservice.persistor.IPersistorService;
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
	private IDPageInstanceService pageInstanceService;

	@Autowired
	private IPersistorService persistorService;

	@Autowired(required = false)
	private IOtherAuthenticationService otherAuthenticationService;

	// ------------------------------ IApplicationLifecycle METHODS

	@Override
	public void init() {
		storePrivilegeKeys();

		system = userService.createOrUpdateUser(
			new UserInputVO("system", null, "", "system", EAuthMechanism.DATABASE)
				.setStatus(EUserStatus.DISABLED)
				.setRowMod(ERowMod.SYSTEM)
		);
		authenticate(system);

		userService.createOrUpdateUser(
			new UserInputVO("root", "root", "", "root", EAuthMechanism.DATABASE)
				.setAdmin(true)
				.setRowMod(ERowMod.SYSTEM)
		);

		guest = userService.createOrUpdateUser(
			new UserInputVO("guest", null, "", "guest", EAuthMechanism.DATABASE)
				.setStatus(EUserStatus.DISABLED)
				.setRowMod(ERowMod.SYSTEM)
		);

		if (!ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity)) {
			guest.setAuthenticated(true);
			guest.setPageVO(pageInstanceService.getDefaultPages());
		}

		roleService.createOrUpdateRole("User", ERowMod.ROOT, true);
		roleService.createOrUpdateRole("Admin", ERowMod.ROOT, true);
		roleService.createOrUpdateRole("Root", ERowMod.SYSTEM, true);

		roleService.createOrUpdateRole("AuthByDB", ERowMod.ROOT, true);
		roleService.createOrUpdateRole("AuthByLDAP", ERowMod.ROOT, true);
		roleService.createOrUpdateRole("AuthByOther", ERowMod.ROOT, true);
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

	// ------------------------------

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

	// ------------------------------

	private void storePrivilegeKeys() {
		Collection<XModule> xModules = ModuleLoader.getModules().values();
		for (XModule xModule : xModules) {
			try {
				String privilegeKeyClass = xModule.getPrivilegeKeyClass();
				if (privilegeKeyClass != null) {
					Class<?> enumClass = Class.forName(privilegeKeyClass);
					if (enumClass.isEnum()) {
						Object[] enumConstants = enumClass.getEnumConstants();
						for (Object enumConstant : enumConstants) {
							IPrivilegeKey key = (IPrivilegeKey) enumConstant;
							key.setModule(xModule.getShortName().toLowerCase());

							checkAndSavePrivilegeKey(key.getName());
						}
					} else {
						throw new DSystemException("IPrivilegeKey class must be enum for module: " + xModule.getShortName());
					}
				}
			} catch (Exception e) {
				logger.error(String.format("Loading module [%s] privilege keys", xModule.getShortName()), e);
			}
		}

		persistorService.commitOrRollback();

		if (logger.isDebugEnabled()) {
			List<Privilege> list = persistorService.list(Privilege.class);
			for (Privilege privilege : list) {
				logger.debug("PrivilegeKey = {}", privilege);
			}
		}
	}

	private void checkAndSavePrivilegeKey(String name) {
		long cnt = persistorService.createQueryBuilder()
			.addSelect("select count(1)")
			.addFrom(Privilege.class, "ent")
			.addWhere("and ent.name = :name")
			.addParam("name", name)
			.object();

		if (cnt == 0) {
			logger.info("Adding PrivilegeKey = {}", name);

			Privilege privilege = new Privilege();
			privilege.setName(name);
			persistorService.saveOrUpdate(privilege);
		}
	}


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

		authenticatedUserVO.addRole(roleService.loadByName("User"));
		if (authenticatedUserVO.isAdmin()) {
			authenticatedUserVO.addRole(roleService.loadByName("Admin"));
		}

		if (authenticatedUserVO.isRoot()) {
			authenticatedUserVO.addRole(roleService.loadByName("Root"));
		}

		if (EAuthMechanism.DATABASE.equals(authenticatedUserVO.getAuthMechanism())) {
			authenticatedUserVO.addRole(roleService.loadByName("AuthByDB"));
		} else if (EAuthMechanism.LDAP.equals(authenticatedUserVO.getAuthMechanism())) {
			authenticatedUserVO.addRole(roleService.loadByName("AuthByLDAP"));
		} else if (EAuthMechanism.OTHER.equals(authenticatedUserVO.getAuthMechanism())) {
			authenticatedUserVO.addRole(roleService.loadByName("AuthByOther"));
		}
		CURRENT_USER.set(authenticatedUserVO);

		logger.info("User Roles: [{}]={}", authenticatedUserVO, authenticatedUserVO.getRoles());
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
