package com.insrb.app.insurance.hi;

import com.insrb.app.exception.WWException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hi_3_PreventOfDenial {

	// 11.	부인방지
	public static String fn_prevent_of_denial(String X_Session_Id, JSONObject data) throws WWException {
		String commonToken = HiToken.GetCommonBearerToken();

		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OACO0100M06S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("X-Session-Id", X_Session_Id)
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(data.toString())
			.asJson();
		JSONObject json = res.getBody().getObject();
		log.debug("fn_prevent_of_denial:" + json.toString());

		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");
			log.debug("fn_1_CertConfmApi:" + json.toString());
			log.debug("oaImgViewUrl:" + json.getString("oaImgViewUrl"));
			String url = json.getString("oaImgViewUrl");
			return url.replace("dB2C/data/", "");
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}
}
