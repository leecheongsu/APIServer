package com.insrb.app.insurance.hi;

import com.insrb.app.exception.WWException;
import java.util.HashMap;
import java.util.Map;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

//ref : http://kong.github.io/unirest-java/#requests

@Slf4j
@Component
public class HiWindWaterInsurance {

	@Value("${hi.waterwind.ww.token.url}")
	private String wwTokenUrl;

	@Value("${hi.waterwind.ww.premium.url}")
	private String premiumUrl;

	@Value("classpath:basic/temp_preminum_req_body.json")
	private Resource tempPreminumReqBodyJson;

	@Value("classpath:basic/temp_cert_confm_api.json.json")
	private Resource tempCertConfmApi_json;

	// 풍수해 인증키
	public String getWWToken() {
		log.info("wwTokenUrl:" + wwTokenUrl);
		HttpResponse<JsonNode> res = Unirest
			.post(wwTokenUrl)
			.basicAuth("29311b91", "75e365273b4f1d81ea944b1c9a9560ad")
			.header("Origin", "https://insrb.com")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.field("grant_type", "client_credentials")
			.field("client_id", "6a08aba5")
			.field("client_secret", "be085145cd0ab2aa19f668ee777bff9a")
			.field("scope", "web-origins")
			.asJson()
			.ifFailure(
				response -> {
					log.error("Oh No! Status" + response.getStatus());
					response
						.getParsingError()
						.ifPresent(
							e -> {
								log.error("Parsing Exception: ", e);
								log.error("Original body: " + e.getOriginalBody());
							}
						);
				}
			);
		JSONObject json = res.getBody().getObject();
		return json.getString("access_token");
	}

	// 일반 인증키
	public String getCommonToken() {
		log.info("wwTokenUrl:" + wwTokenUrl);
		return Unirest
			.post(wwTokenUrl)
			.basicAuth("29311b91", "75e365273b4f1d81ea944b1c9a9560ad")
			.header("Origin", "https://insrb.com")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.field("grant_type", "client_credentials")
			.field("client_id", "6a08aba5")
			.field("client_secret", "be085145cd0ab2aa19f668ee777bff9a")
			.field("scope", "web-origins")
			.asJson()
			.getBody()
			.getObject()
			.getString("access_token");
	}
	// 2. 가보험료 산출 요청
	public Map<String, Object> getPrePremium(String data) throws WWException {
		String wwToken = getCommonToken();
		// log.info("wwToken:" + wwToken);
		// log.info("addressJson:" + data);
		String auth = "Bearer " + wwToken;
		// return data;
		HttpResponse<JsonNode> res = Unirest
			.post(premiumUrl)
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "https://insrb.com")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("Authorization", auth)
			.header("Content-Type", "application/json")
			.body(data)
			.asJson();
		// .getBody()
		// .getObject()
		// .getString("resultCode");
		JSONObject json = res.getBody().getObject();
		int resultCode =    json.getInt("resultCode");
		if (resultCode != 0) throw new WWException("현대해상 가보험료 요청 결과 오류(오류코드):" + resultCode);
		JSONObject giid0100vo = json.getJSONObject("oagi6002vo").getJSONObject("giid0100vo");
		Map<String, Object> rtn = new HashMap<String, Object>();
		rtn.put("perPrem", giid0100vo.getInt("perPrem"));     //본인 부담 보험료
		rtn.put("govtPrem", giid0100vo.getInt("govtPrem"));   //정부 부담 보험료
		rtn.put("lgovtPrem", giid0100vo.getInt("lgovtPrem")); // 지자체 부담 보험료
		rtn.put("tpymPrem", giid0100vo.getInt("tpymPrem"));   //총보험료
		return rtn;
	}

	// 3.	CertConfmApi() : 본인인증이력등록 API 호출
	public Map<String, Object> certConfmApi(String data){
		return null;
	}
	// 4.	fnPremiumMathApi(): 보험료 계산 API 호출
	public Map<String, Object> fnPremiumMathApi(String data){
		return null;
	}
	// 5.	fnJoinTobeApi(): 가입설계 API 호출
	// 6.	fnProdtManual(): 통합청약서 (상품설명서) API 호출
	// 7.	fnProdtTerms(): 상품약관조회 API 연동

}
