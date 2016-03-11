package org.devocative.demeter.web.component.grid;

import org.devocative.demeter.web.DemeterExceptionToMessageHandler;
import org.devocative.wickomp.grid.ITreeGridDataSource;
import org.devocative.wickomp.grid.OTreeGrid;
import org.devocative.wickomp.grid.WTreeGrid;

public class DTreeGrid<T> extends WTreeGrid<T> {
	public DTreeGrid(String id, OTreeGrid<T> options, ITreeGridDataSource<T> dataSource) {
		super(id, options, dataSource);

		setExceptionMessageHandler(DemeterExceptionToMessageHandler.get());
	}
}
