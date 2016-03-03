package org.devocative.demeter.web.component.grid;

import org.devocative.demeter.web.DemeterExceptionToMessageHandler;
import org.devocative.wickomp.data.WGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;

public class DDataGrid<T> extends WDataGrid<T> {
	public DDataGrid(String id, OGrid<T> options, WGridDataSource<T> dataSource) {
		super(id, options, dataSource);

		setExceptionMessageHandler(DemeterExceptionToMessageHandler.get());
	}
}
