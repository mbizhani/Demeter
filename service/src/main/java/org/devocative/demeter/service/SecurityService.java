package org.devocative.demeter.service;

import org.devocative.adroit.ConfigUtil;
import org.devocative.adroit.StringEncryptorUtil;
import org.devocative.demeter.*;
import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.*;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.devocative.demeter.vo.UserInputVO;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.vo.core.DModuleInfoVO;
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

	private static final ThreadLocal<UserVO> CURRENT_USER = new ThreadLocal<>();

	private UserVO system, guest;

	@Autowired
	private IUserService userService;

	@Autowired
	private IRoleService roleService;

	@Autowired
	private IDPageInstanceService pageInstanceService;

	@Autowired
	private IPersistorService persistorService;

	@Autowired
	private IDemeterCoreService demeterCoreService;

	@Autowired(required = false)
	private IOtherAuthenticationService otherAuthenticationService;

	// ------------------------------ IApplicationLifecycle METHODS

	@Override
	public void init() {
		storePrivilegeKeys();

		system = userService.createOrUpdateUser(
			new UserInputVO("system", null, "", "system", EAuthMechanism.DATABASE)
				.setStatus(EUserStatus.DISABLED)
				.setRowMod(ERowMode.SYSTEM)
				.setSessionTimeout(0),
			null,
			true
		);
		authenticate(system);

		userService.createOrUpdateUser(
			new UserInputVO("root", "root", "", "root", EAuthMechanism.DATABASE)
				.setAdmin(true)
				.setRowMod(ERowMode.SYSTEM),
			null,
			true
		);

		guest = userService.createOrUpdateUser(
			new UserInputVO("guest", null, "", "guest", EAuthMechanism.DATABASE)
				.setStatus(EUserStatus.DISABLED)
				.setRowMod(ERowMode.SYSTEM)
				.setSessionTimeout(-1),
			null,
			true
		);

		if (!ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity)) {
			guest.setAuthenticated(true);
			guest.setPageVO(pageInstanceService.getDefaultPages());
		}

		roleService.createOrUpdateRole("User", ERowMode.ROOT, true);
		roleService.createOrUpdateRole("Admin", ERowMode.ROOT, true);
		roleService.createOrUpdateRole("Root", ERowMode.SYSTEM, true);

		roleService.createOrUpdateRole("AuthByDB", ERowMode.ROOT, true);
		roleService.createOrUpdateRole("AuthByLDAP", ERowMode.ROOT, true);
		roleService.createOrUpdateRole("AuthByOther", ERowMode.ROOT, true);

		persistorService.commitOrRollback();
	}

	@Override
	public void shutdown() {
		CURRENT_USER.remove();
	}

	@Override
	public ApplicationLifecyclePriority getLifecyclePriority() {
		return ApplicationLifecyclePriority.Second;
	}

	// --------------- IRequestLifecycle implementation

	@Override
	public void beforeRequest() {
	}

	@Override
	public void afterResponse() {
		CURRENT_USER.remove();
	}

	// ---------------

	@Override
	public UserVO getCurrentUser() {
		return CURRENT_USER.get();
	}

	@Override
	public void authenticate(UserVO userVO) {
		//TODO some checking, and maybe reloading the user's data
		if (userVO != null) {
			CURRENT_USER.set(userVO);
			DLogCtx.put("user", userVO.getUsername());
		} else {
			throw new DemeterException(DemeterErrorCode.InvalidUser);
		}
	}

	@Override
	public void authenticate(String username, String password) {
		resetToGuest();

		UserVO authenticatedUserVO = null;
		User user = userService.loadByUsername(username);

		if (user != null) {
			logger.info("Authenticating: username[{}] in DB, status=[{}], auth=[{}]",
				username, user.getStatus(), user.getAuthMechanism());

			verifyUser(user);

			if (EAuthMechanism.DATABASE.equals(user.getAuthMechanism())) {
				authenticatedUserVO = authenticateByDatabase(user, password);
			} else if (EAuthMechanism.LDAP.equals(user.getAuthMechanism())) {
				authenticatedUserVO = authenticateByLDAP(username, password, user);
			} else if (EAuthMechanism.OTHER.equals(user.getAuthMechanism())) {
				if (otherAuthenticationService != null) {
					if (ConfigUtil.getBoolean(DemeterConfigKey.OtherAuthUserPassEnabled)) {
						authenticatedUserVO = authenticateByOther(username, password, user);
					} else {
						throw new DSystemException("OtherAuthenticationService not enabled for user/pass authentication: user = " + username);
					}
				} else {
					throw new DSystemException("No IOtherAuthenticationService bean defined: user = " + username);
				}
			} else {
				throw new DSystemException(String.format("Invalid authenticate mechanism: username=[%s] authMethod=[%s]",
					username, user.getAuthMechanism()));
			}
		} else {
			Boolean autoRegister = ConfigUtil.getBoolean(DemeterConfigKey.UserAutoRegister);
			logger.info("Authenticating: username[{}] not in DB, autoRegister=[{}]", username, autoRegister);

			if (autoRegister) {
				if (ConfigUtil.hasKey(DemeterConfigKey.LdapUrl)) {
					authenticatedUserVO = authenticateByLDAP(username, password, null);
				}

				if (authenticatedUserVO == null &&
					otherAuthenticationService != null &&
					ConfigUtil.getBoolean(DemeterConfigKey.OtherAuthUserPassEnabled)) {

					authenticatedUserVO = authenticateByOther(username, password, null);
				}

				if (authenticatedUserVO == null) {
					throw new DemeterException(DemeterErrorCode.InvalidUser);
				}
			} else {
				throw new DemeterException(DemeterErrorCode.InvalidUser);
			}
		}

		afterAuthentication(authenticatedUserVO);
	}

	@Override
	public UserVO authenticateByUrlParams(Map<String, List<String>> params) {
		if (otherAuthenticationService != null && otherAuthenticationService.canProceedAuthentication(params)) {
			resetToGuest();

			UserInputVO authUserInputVO = otherAuthenticationService.authenticate(params);

			if (authUserInputVO != null) {
				User user = userService.loadByUsername(authUserInputVO.getUsername());
				if (user != null) {
					verifyUser(user);
				}
				UserVO authUserVO = userService.createOrUpdateUser(authUserInputVO, user, ConfigUtil.getBoolean(DemeterConfigKey.OtherAuthUpdate));

				logger.info("Authenticate by URL: user=[{}] roles=[{}] permissions={} denials={}",
					authUserVO.getUsername(),
					authUserVO.getRoles(),
					authUserVO.getPermissions(),
					authUserVO.getDenials()
				);

				afterAuthentication(authUserVO);

				return authUserVO;
			}
		}

		return null;
	}

	@Override
	public void signOut() {
		CURRENT_USER.set(guest);
	}

	/*@Override
	public String getUserDigest(String username) {
		//NOTE: the password must be saved symmetric-encoded or the following hash must be persisted somewhere
		User user = userService.loadByUsername(username);

		if (user != null) {
			return DigestUtils.md5Hex(
				user.getUsername() + ":" +
					ConfigUtil.getString(DemeterConfigKey.SecurityRealm) + ":" +
					user.getPassword());
		}

		return null;
	}*/

	@Override
	public UserVO getSystemUser() {
		if (system == null) {
			throw new RuntimeException("Can't find UserVO of 'system' ");
		}
		return system;
	}

	@Override
	public UserVO getGuestUser() {
		if (guest == null) {
			throw new RuntimeException("Can't find UserVO of 'guest' ");
		}
		return guest;
	}

	// ------------------------------

	private void storePrivilegeKeys() {
		Collection<DModuleInfoVO> xModules = demeterCoreService.getModules();
		for (DModuleInfoVO xModule : xModules) {
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

	// ---------------

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

	private UserVO authenticateByLDAP(String username, String password, User eqUserInDB) {
		logger.info("Authenticating: by LDAP username = [{}]", username);

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
			String lastName = null;
			try {
				firstName = getValue(attrs.get(ConfigUtil.getString(DemeterConfigKey.LdapAttrFirstName)));
				lastName = getValue(attrs.get(ConfigUtil.getString(DemeterConfigKey.LdapAttrLastName)));
			} catch (Exception e) {
				logger.error("Getting first name & last name from LDAP attributes", e);
			}

			logger.info("Authenticated by LDAP: username=[{}]", username);

			boolean forceUpdate = eqUserInDB != null && (
				(firstName != null && !firstName.equals(eqUserInDB.getPerson().getFirstName())) ||
					(lastName != null && !lastName.equals(eqUserInDB.getPerson().getLastName()))
			);
			return userService.createOrUpdateUser(new UserInputVO(username, firstName, lastName, EAuthMechanism.LDAP), eqUserInDB, forceUpdate);
		} catch (AuthenticationException e) {
			logger.warn("Authentication By LDAP failed for user: {}", username);
			throw new DemeterException(DemeterErrorCode.InvalidUser);
		} catch (NamingException e) {
			logger.error("Authentication by LDAP error: ", e);
			throw new DSystemException("LDAP Server Problem: ", e);
		}
	}

	private UserVO authenticateByOther(String username, String password, User eqUserInDB) {
		Map<String, List<String>> params = new HashMap<>();
		params.put(ConfigUtil.getString(DemeterConfigKey.OtherAuthUsernameParam), Collections.singletonList(username));
		params.put(ConfigUtil.getString(DemeterConfigKey.OtherAuthPasswordParam), Collections.singletonList(password));
		UserInputVO userInputVO = otherAuthenticationService.authenticate(params);

		if (userInputVO == null) {
			throw new DemeterException(DemeterErrorCode.InvalidUser);
		}

		logger.info("Authenticated by Other: username=[{}]", username);
		return userService.createOrUpdateUser(userInputVO, eqUserInDB, ConfigUtil.getBoolean(DemeterConfigKey.OtherAuthUpdate));
	}

	private void afterAuthentication(UserVO authenticatedUserVO) {
		userService.updateLastLoginDate(authenticatedUserVO.getUsername());

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

	private void resetToGuest() {
		CURRENT_USER.set(guest);
	}
}
