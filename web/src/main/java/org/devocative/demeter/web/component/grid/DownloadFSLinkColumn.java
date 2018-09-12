package org.devocative.demeter.web.component.grid;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ContentDisposition;
import org.devocative.demeter.web.DemeterIcon;
import org.devocative.demeter.web.UrlUtil;
import org.devocative.wickomp.grid.column.OColumn;
import org.devocative.wickomp.html.HTMLBase;

public abstract class DownloadFSLinkColumn<T> extends OColumn<T> {
	private static final long serialVersionUID = 15668724728385694L;

	private ContentDisposition disposition;
	private HTMLBase icon = DemeterIcon.DOWNLOAD;

	// ------------------------------

	public DownloadFSLinkColumn() {
		super(new Model<>(), "_DOWNLOAD_");
	}

	// ------------------------------

	public DownloadFSLinkColumn<T> setDisposition(ContentDisposition disposition) {
		this.disposition = disposition;
		return this;
	}

	public DownloadFSLinkColumn<T> setIcon(HTMLBase icon) {
		this.icon = icon;
		return this;
	}

	// ------------------------------

	protected abstract String getFileId(T bean);

	// ------------------------------

	@Override
	public final String cellValue(T bean, String id, int colNo, String url) {
		return String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
			UrlUtil.getFileUri(getFileId(bean), disposition), icon.toString());
	}
}
