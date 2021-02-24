package com.insrb.app.insurance.hi;

import com.insrb.app.exception.WWException;
import java.util.HashMap;
import java.util.Map;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hi_1_PrePremium {

	// 가보험료 산출 요청
	public static Map<String, Object> GetPrePremium(String data) throws WWException {
		String wwToken = HiToken.GetWWBearerToken();
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OASF2001M05S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", wwToken)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(data)
			.asJson();

		JSONObject json = res.getBody().getObject();
		log.info("GetPrePremium:" + json.toString());

		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
			JSONObject giid0100vo = json.getJSONObject("oagi6002vo").getJSONObject("giid0100vo");
			Map<String, Object> rtn = new HashMap<String, Object>();
			rtn.put("perPrem", giid0100vo.getInt("perPrem")); //본인 부담 보험료
			rtn.put("govtPrem", giid0100vo.getInt("govtPrem")); //정부 부담 보험료
			rtn.put("lgovtPrem", giid0100vo.getInt("lgovtPrem")); // 지자체 부담 보험료
			rtn.put("tpymPrem", giid0100vo.getInt("tpymPrem")); //총보험료
			return rtn;
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}
}
