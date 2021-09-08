package com.insrb.app.utils;

import kong.unirest.json.JSONObject;

public class InsuJsonUtil {

	public static double IntOrDoubleToDouble(Object item) {
		if (item instanceof Integer) {
			return (int) item;
		} else if (item instanceof Double) {
			return (double) item;
		}
		return 0.0;
	}

	public static String IfNullDefault(JSONObject json, String key, String dflt) {
		try {
			return json.getString(key);
		} catch (Exception e) {
			return dflt;
		}
	}
}
