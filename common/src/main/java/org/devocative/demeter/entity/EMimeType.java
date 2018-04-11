package org.devocative.demeter.entity;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;

public enum EMimeType {
	BINARY(1, "Binary", "application/binary"),
	// Microsoft
	EXCEL(10, "Microsoft Excel", "application/vnd.ms-excel"),
	// Text
	TEXT(30, "Text", "text/plain"),
	HTML(31, "HTML", "text/html"),
	XML(32, "XML", "text/xml"),
	// IMAGE
	IMAGE(50, "Image", "image"),
	IMAGE_PNG(51, "PNG Image", "image/png"),
	IMAGE_JPEG(52, "JPEG Image", "image/jpeg"),
	IMAGE_GIF(53, "GIF Image", "image/gif"),
	// VIDEO
	VIDEO(70, "Video", "video"),
	// COMPRESS
	COMPRESS(90, "Compress", "application/x-compress"),
	ZIP(91, "Zip", "application/zip"),
	GZIP(92, "GZip", "application/x-gzip");

	// ------------------------------

	private Integer id;
	private String name;
	private String type;

	// ------------------------------

	EMimeType(Integer id, String name, String type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	// ------------------------------

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	// ---------------

	@Override
	public String toString() {
		return getName();
	}

	// ------------------------------

	public static List<EMimeType> list() {
		return Arrays.asList(values());
	}

	// ------------------------------

	public static class Converter implements AttributeConverter<EMimeType, Integer> {
		@Override
		public Integer convertToDatabaseColumn(EMimeType eMimeType) {
			return eMimeType != null ? eMimeType.getId() : null;
		}

		@Override
		public EMimeType convertToEntityAttribute(Integer integer) {
			for (EMimeType literal : values()) {
				if (literal.getId().equals(integer)) {
					return literal;
				}
			}
			return null;
		}
	}
}
