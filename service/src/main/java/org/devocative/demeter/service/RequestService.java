package org.devocative.demeter.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.devocative.demeter.iservice.IRequestService;
import org.devocative.demeter.vo.RequestVO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Service("dmtRequestService")
public class RequestService implements IRequestService {
	private static final ThreadLocal<RequestVO> CURRENT_REQUEST = new ThreadLocal<>();

	// ------------------------------

	@Override
	public RequestVO getCurrentRequest() {
		return CURRENT_REQUEST.get();
	}

	@Override
	public void set(RequestVO requestVO) {
		CURRENT_REQUEST.set(requestVO);
	}

	@Override
	public void unset() {
		CURRENT_REQUEST.remove();
	}

	// ---------------

	@Override
	public String toJson(Object obj) {
		return toJson(obj, null, null);
	}

	@Override
	public String toJson(Object obj, Map<JsonParser.Feature, Boolean> features, Map<SerializationFeature, Boolean> serializationFeatures) {
		StringWriter sw = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

		if (features != null) {
			for (Map.Entry<JsonParser.Feature, Boolean> entry : features.entrySet()) {
				mapper.configure(entry.getKey(), entry.getValue());
			}
		}

		if (serializationFeatures != null) {
			for (Map.Entry<JsonParser.Feature, Boolean> entry : features.entrySet()) {
				mapper.configure(entry.getKey(), entry.getValue());
			}
		}

		try {
			mapper.writeValue(sw, obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	@Override
	public <T> T fromJson(String json, Class<T> cls) {
		return fromJson(json, cls, null, null);
	}

	@Override
	public <T> T fromJson(String json, Class<T> cls, Map<JsonParser.Feature, Boolean> features, Map<DeserializationFeature, Boolean> deserializationFeatures) {
		ObjectMapper mapper = createMapper(features, deserializationFeatures);

		try {
			return mapper.readValue(json, cls);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T fromJson(String json, TypeReference<T> typeReference) {
		return fromJson(json, typeReference, null, null);
	}

	@Override
	public <T> T fromJson(String json, TypeReference<T> typeReference, Map<JsonParser.Feature, Boolean> features, Map<DeserializationFeature, Boolean> deserializationFeatures) {
		ObjectMapper mapper = createMapper(features, deserializationFeatures);

		try {
			return mapper.readValue(json, typeReference);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	private ObjectMapper createMapper(Map<JsonParser.Feature, Boolean> features, Map<DeserializationFeature, Boolean> deserializationFeatures) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		if (features != null) {
			for (Map.Entry<JsonParser.Feature, Boolean> entry : features.entrySet()) {
				mapper.configure(entry.getKey(), entry.getValue());
			}
		}

		if (deserializationFeatures != null) {
			for (Map.Entry<DeserializationFeature, Boolean> entry : deserializationFeatures.entrySet()) {
				mapper.configure(entry.getKey(), entry.getValue());
			}
		}
		return mapper;
	}


}
