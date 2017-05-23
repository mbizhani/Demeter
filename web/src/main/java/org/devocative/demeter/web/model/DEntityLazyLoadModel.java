package org.devocative.demeter.web.model;

import org.apache.wicket.model.LoadableDetachableModel;
import org.devocative.demeter.iservice.IEntityService;

public class DEntityLazyLoadModel<T> extends LoadableDetachableModel<T> {
	private static final long serialVersionUID = 8583754573795679493L;

	// ------------------------------

	private Long id;
	private IEntityService<T> entityService;

	// ------------------------------

	public DEntityLazyLoadModel(Long id, IEntityService<T> entityService) {
		this.id = id;
		this.entityService = entityService;
	}

	// ------------------------------

	@Override
	protected T load() {
		return entityService.load(id);
	}
}
