package org.devocative.demeter.service;

import org.devocative.demeter.entity.*;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class HibernateInterceptor extends EmptyInterceptor {
	private static final long serialVersionUID = -820555101887857570L;
	private static Logger logger = LoggerFactory.getLogger(HibernateInterceptor.class);

	private ISecurityService securityService;

	// ------------------------------

	public HibernateInterceptor(ISecurityService securityService) {
		this.securityService = securityService;
	}

	// ------------------------------

	// insert
	public boolean onSave(
		Object entity,
		Serializable id,
		Object[] state,
		String[] propertyNames,
		Type[] types) {

		boolean result = false;

		if (entity instanceof ICreationDate ||
			entity instanceof ICreatorUser) {
			setCreatedValues(entity, id, state, propertyNames);
			result = true;
		}

		if (entity instanceof IModificationDate || entity instanceof IModifierUser) {
			setModifiedValues(entity, id, state, propertyNames);
			result = true;
		}

		if (entity instanceof IRowMode && entity instanceof IRoleRowAccess) {
			result = setRowLevelAccess(entity, id, state, propertyNames) || result;
		}

		return result;
	}

	// update
	public boolean onFlushDirty(
		Object entity,
		Serializable id,
		Object[] currentState,
		Object[] previousState,
		String[] propertyNames,
		Type[] types) {

		boolean result = false;

		if (entity instanceof IModificationDate ||
			entity instanceof IModifierUser) {
			setModifiedValues(entity, id, currentState, propertyNames);
			result = true;
		}

		if (entity instanceof IRowMode && entity instanceof IRoleRowAccess) {
			result = setRowLevelAccess(entity, id, currentState, propertyNames) || result;
		}

		return result;
	}

	// ------------------------------

	private void setCreatedValues(Object entity, Serializable id, Object[] state, String[] propertyNames) {
		for (int i = 0; i < propertyNames.length; i++) {
			if ("creatorUserId".equals(propertyNames[i])) {
				if (securityService != null && securityService.getCurrentUser() != null) {
					state[i] = securityService.getCurrentUser().getUserId();
				} else {
					logger.warn("Hibernate.Interceptor for creatorUserId: invalid currentUser, entity=[{}] id=[{}]", entity.getClass().getName(), id);
					if (entity instanceof Person) {
						Person p = (Person) entity;
						if (!"system".equals(p.getLastName())) {
							throw new RuntimeException("Invalid CurrentUser");
						}
					} else {
						throw new RuntimeException("Invalid CurrentUser");
					}
				}
			} else if ("creationDate".equals(propertyNames[i])) {
				state[i] = new Date();
			}
		}
	}

	private void setModifiedValues(Object entity, Serializable id, Object[] state, String[] propertyNames) {
		for (int i = 0; i < propertyNames.length; i++) {
			if ("modifierUserId".equals(propertyNames[i])) {
				if (securityService != null && securityService.getCurrentUser() != null) {
					state[i] = securityService.getCurrentUser().getUserId();
				} else {
					logger.error("Hibernate.Interceptor for creatorUserId: invalid currentUser, entity=[{}] id=[{}]", entity.getClass().getName(), id);
					if (entity instanceof Person) {
						Person p = (Person) entity;
						if (!"system".equals(p.getLastName())) {
							throw new RuntimeException("Invalid CurrentUser");
						}
					} else {
						throw new RuntimeException("Invalid CurrentUser");
					}
				}
			} else if ("modificationDate".equals(propertyNames[i])) {
				state[i] = new Date();
			}
		}
	}

	private boolean setRowLevelAccess(Object entity, Serializable id, Object[] state, String[] propertyNames) {
		IRowMode rowMode = (IRowMode) entity;
		IRoleRowAccess roleRowAccess = (IRoleRowAccess) entity;

		if (ERowMode.ROLE.equals(rowMode.getRowMode())) {
			for (int i = 0; i < propertyNames.length; i++) {
				if ("allowedRoles".equals(propertyNames[i])) {
					if (roleRowAccess.getAllowedRoles() == null || roleRowAccess.getAllowedRoles().isEmpty()) {
						UserVO currentUser = securityService.getCurrentUser();
						if (currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().size() > 0) {
							List<Role> roles = new ArrayList<>();
							for (Role role : currentUser.getRoles()) {
								if (!ERoleMode.DYNAMIC.equals(role.getRoleMode())) {
									roles.add(role);
									break;
								}
							}
							state[i] = roles;
							return true;
						} else {
							logger.error("Invalid current user & roles: user={}, entity={}, id={}",
								currentUser, entity.getClass().getName(), id);
						}
					}
					break;
				}
			}
		}

		return false;
	}
}
