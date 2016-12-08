package org.devocative.demeter.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EMimeType implements Serializable {
	private static final long serialVersionUID = -3231399031464989575L;

	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
	private static final Map<Integer, String> ID_TO_TYPE = new HashMap<>();
	private static final List<EMimeType> ALL = new ArrayList<>();

	// ------------------------------

	public static final EMimeType BINARY = new EMimeType(1, "Binary", "application/binary");
	// Microsoft
	public static final EMimeType EXCEL = new EMimeType(10, "Microsoft Excel", "application/vnd.ms-excel");
	// Text
	public static final EMimeType TEXT = new EMimeType(30, "Text", "text/plain");
	public static final EMimeType HTML = new EMimeType(31, "HTML", "text/html");
	// IMAGE
	public static final EMimeType IMAGE = new EMimeType(50, "Image", "image");
	public static final EMimeType IMAGE_PNG = new EMimeType(51, "PNG Image", "image/png");
	public static final EMimeType IMAGE_JPEG = new EMimeType(52, "JPEG Image", "image/jpeg");
	public static final EMimeType IMAGE_GIF = new EMimeType(53, "GIF Image", "image/gif");
	// VIDEO
	public static final EMimeType VIDEO = new EMimeType(70, "Video", "video");
	// COMPRESS
	public static final EMimeType COMPRESS = new EMimeType(90, "Compress", "application/x-compress");
	public static final EMimeType ZIP = new EMimeType(91, "Zip", "application/zip");
	public static final EMimeType GZIP = new EMimeType(92, "GZip", "application/x-gzip");

	// ------------------------------

	private Integer id;

	// ------------------------------

	public EMimeType(Integer id, String name, String type) {
		this.id = id;

		ID_TO_NAME.put(id, name);
		ID_TO_TYPE.put(id, type);
		ALL.add(this);
	}

	public EMimeType() {
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return ID_TO_NAME.get(getId());
	}

	public String getType() {
		return ID_TO_TYPE.get(getId());
	}

	// ------------------------------

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EMimeType)) return false;

		EMimeType eMimeType = (EMimeType) o;

		return !(getId() != null ? !getId().equals(eMimeType.getId()) : eMimeType.getId() != null);

	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<EMimeType> list() {
		return new ArrayList<>(ALL);
	}
}
