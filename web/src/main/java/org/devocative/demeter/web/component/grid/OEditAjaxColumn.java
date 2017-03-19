package org.devocative.demeter.web.component.grid;

import org.apache.wicket.model.Model;
import org.devocative.demeter.web.DemeterIcon;

public abstract class OEditAjaxColumn<T> extends ORowModAjaxColumn<T> {
	private static final long serialVersionUID = -1897049038417378148L;

	public OEditAjaxColumn() {
		super(new Model<String>(), DemeterIcon.EDIT);
		setField("EDIT");
	}
}
