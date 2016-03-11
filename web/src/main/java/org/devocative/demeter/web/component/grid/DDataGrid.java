package org.devocative.demeter.web.component.grid;

import org.devocative.demeter.web.DemeterExceptionToMessageHandler;
import org.devocative.wickomp.grid.IGridDataSource;
import org.devocative.wickomp.grid.OGrid;
import org.devocative.wickomp.grid.WDataGrid;

public class DDataGrid<T> extends WDataGrid<T> {
	public DDataGrid(String id, OGrid<T> options, IGridDataSource<T> dataSource) {
		super(id, options, dataSource);

		setExceptionMessageHandler(DemeterExceptionToMessageHandler.get());
	}
}
