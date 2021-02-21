package com.insrb.app.insurance.hi;

import com.insrb.app.exception.WWException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HiToken {

	private static final String SCOPE = "web-origins";
	private static final String GRANT_TYPE = "client_credentials";
	private static final String ORIGIN = "https://insrb.com";

	// 풍수해 인증키
	public static String GetWWBearerToken() throws WWException {
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SSO_SERVER + "/auth/realms/3scale/protocol/openid-connect/token")
			.basicAuth(HiConfig.WW_CLIENT_ID, HiConfig.WW_CLIENT_SECRET)
			.header("Origin", ORIGIN)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.field("grant_type", GRANT_TYPE)
			.field("scope", SCOPE)
			.asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			return "Bearer " + json.getString("access_token");
		} else {
			log.error("GetWWBearerToken:" + res.getStatusText());
			throw new WWException(res.getStatusText());
		}
	}

	// 일반 인증키
	public static String GetCommonBearerToken() throws WWException {
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SSO_SERVER + "/auth/realms/3scale/protocol/openid-connect/token")
			.basicAuth(HiConfig.COMMON_CLIENT_ID, HiConfig.COMMON_CLIENT_SECRET)
			.header("Origin", ORIGIN)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.field("grant_type", GRANT_TYPE)
			.field("scope", SCOPE)
			.asJson();
		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			return "Bearer " + json.getString("access_token");
		} else {
			log.error("GetCommonBearerToken:" + res.getStatusText());
			throw new WWException(res.getStatusText());
		}
	}
}
