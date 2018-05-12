package org.devocative.demeter.web.component.grid;

import org.apache.wicket.model.Model;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.wickomp.grid.column.OColumn;

public abstract class DownloadFSLinkColumn<T> extends OColumn<T> {
	private static final long serialVersionUID = 15668724728385694L;

	// ------------------------------

	public DownloadFSLinkColumn() {
		super(new Model<>(), "_DOWNLOAD_");
	}

	// ------------------------------

	protected abstract String getFileId(T bean);

	// ------------------------------

	@Override
	public final String cellValue(T bean, String id, int colNo, String url) {
		return String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
			UrlUtil.getFileUri(getFileId(bean)), DemeterIcon.DOWNLOAD.toString());
	}

	@Override
	public final String footerCellValue(Object bean, int colNo, String url) {
		return null;
	}
}
