package org.devocative.demeter.iservice;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.devocative.demeter.vo.RequestVO;

import java.util.Map;

public interface IRequestService {
	RequestVO getCurrentRequest();

	void set(RequestVO requestVO);

	void unset();

	String toJson(Object obj);

	String toJson(Object obj, Map<JsonParser.Feature, Boolean> features, Map<SerializationFeature, Boolean> serializationFeatures);

	<T> T fromJson(String json, Class<T> cls);

	<T> T fromJson(String json, Class<T> cls, Map<JsonParser.Feature, Boolean> features, Map<DeserializationFeature, Boolean> deserializationFeatures);

	<T> T fromJson(String json, TypeReference<T> typeReference);

	<T> T fromJson(String json, TypeReference<T> typeReference, Map<JsonParser.Feature, Boolean> features, Map<DeserializationFeature, Boolean> deserializationFeatures);
}
