package com.insrb.app.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import kong.unirest.json.JSONObject;

public class ResourceUtil {

	// mock Resource to String
	public static String asString(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.getInputStream(), "UTF-8")) {
			return FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static Map<String, Object> asMap(Resource resource) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = objectMapper.readValue(resource.getInputStream(), new TypeReference<Map<String, Object>>() {});
			return map;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static JSONObject asJSONObject(Resource resource)  {
		Map<String, Object> map = ResourceUtil.asMap(resource);
		JSONObject obj = new JSONObject(map);
		return obj;
	}
}
