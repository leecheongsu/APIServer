package com.insrb.app.insurance.hi;

import com.insrb.app.exception.WWException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hi_4_Order {

	// 청약확정
	public static JSONObject FnConfirmsubscription(String X_Session_Id, JSONObject data) throws WWException {
		String wwToken = HiToken.GetWWBearerToken();
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OASF2001M12S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", wwToken)
			.header("X-Session-Id", X_Session_Id)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(data.toString())
			.asJson();
		JSONObject json = res.getBody().getObject();
		log.info("fn_fnConfirmsubscription:" + json.toString());
		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");
			return json.getJSONObject("giid0410vo");
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}
}
