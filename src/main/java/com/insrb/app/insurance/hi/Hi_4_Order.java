package com.insrb.app.insurance.hi;

import com.insrb.app.exception.WWException;
import com.insrb.app.util.ResourceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hi_4_Order {

	// 청약확정
	public static String FnConfirmsubscription(JSONObject data) throws WWException {
		String wwToken = HiToken.GetWWBearerToken();
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OASF2001M12S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", wwToken)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(data.toString())
			.asJson();
		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 청약확정 API 호출 결과 오류(오류코드):" + resultCode);
			log.info("fn_fnConfirmsubscription:" + json.toString());
			return "OK";
		} else {
			throw new WWException(res.getStatusText());
		}
	}
}
