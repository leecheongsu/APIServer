package com.insrb.app.insurance.hi;

import java.util.HashMap;
import java.util.Map;
import com.insrb.app.exception.WWException;
import com.insrb.app.mapper.WPremcalDetailMapper;
import com.insrb.app.util.ResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

//ref : http://kong.github.io/unirest-java/#requests

@Slf4j
@Component
public class HiWindWaterInsurance {

	// @Value("${hi.waterwind.sso_server}")
	// private String sso_server;

	// @Value("${hi.waterwind.server}")
	// private String server;

	@Value("classpath:basic/tmpl_preminum_req_body.json")
	private Resource tmplPreminumReqBody_json;

	@Value("classpath:basic/tmpl_cert_confm_api.json")
	private Resource tmplCertConfmApi_json;

	@Value("classpath:basic/join_tobe.json")
	private Resource joinTobe_json;

	@Value("classpath:basic/prodt_manual.json")
	private Resource prodtManual_json;

	@Autowired
	WPremcalDetailMapper wPremcalDetailMapper;

	// 개발
	// private String sso_server = "https://apid-sso.hi.co.kr";
	// private String server = "https://apid.hi.co.kr";
	// private String ww_client_id = "29311b91";
	// private String ww_client_secret = "75e365273b4f1d81ea944b1c9a9560ad";
	// private String common_client_id = "6a08aba5";
	// private String common_client_secret = "be085145cd0ab2aa19f668ee777bff9a";
	// 운영
	private String sso_server = "https://api-sso.hi.co.kr";
	private String server = "https://api.hi.co.kr";
	private String ww_client_id = "53dca6a6";
	private String ww_client_secret = "5a838fc51a4015d030f19770f78fc143";
	private String common_client_id = "60b111e8";
	private String common_client_secret = "9241b101f32b482853a8ea40c7ec8ba2";

	// 풍수해 인증키
	private String getWWBearerToken() throws WWException {
		HttpResponse<JsonNode> res = Unirest
			.post(sso_server + "/auth/realms/3scale/protocol/openid-connect/token")
			.basicAuth(ww_client_id, ww_client_secret)
			.header("Origin", "https://insrb.com")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.field("grant_type", "client_credentials")
			.field("scope", "web-origins")
			.asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			return "Bearer " + json.getString("access_token");
		} else {
			log.error("getWWBearerToken:" + res.getStatusText());
			throw new WWException(res.getStatusText());
		}
	}

	// 일반 인증키
	private String getCommonBearerToken() throws WWException {
		HttpResponse<JsonNode> res = Unirest
			.post(sso_server + "/auth/realms/3scale/protocol/openid-connect/token")
			.basicAuth(common_client_id, common_client_secret)
			.header("Origin", "https://insrb.com")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.field("grant_type", "client_credentials")
			.field("scope", "web-origins")
			.asJson();
		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			return "Bearer " + json.getString("access_token");
		} else {
			log.error("getCommonBearerToken:" + res.getStatusText());
			throw new WWException(res.getStatusText());
		}
	}

	// 2. 가보험료 산출 요청
	public Map<String, Object> getPrePremium(String data) throws WWException {
		String wwToken = getWWBearerToken();
		HttpResponse<JsonNode> res = Unirest
			.post(server + "/v1/OASF2001M05S")
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "210.179.172.177")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("Authorization", wwToken)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(data)
			.asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 가보험료 요청 결과 오류(오류코드):" + resultCode);
			JSONObject giid0100vo = json.getJSONObject("oagi6002vo").getJSONObject("giid0100vo");
			Map<String, Object> rtn = new HashMap<String, Object>();
			rtn.put("perPrem", giid0100vo.getInt("perPrem")); //본인 부담 보험료
			rtn.put("govtPrem", giid0100vo.getInt("govtPrem")); //정부 부담 보험료
			rtn.put("lgovtPrem", giid0100vo.getInt("lgovtPrem")); // 지자체 부담 보험료
			rtn.put("tpymPrem", giid0100vo.getInt("tpymPrem")); //총보험료
			return rtn;
		} else {
			log.error("getPrePremium:" + res.getStatusText());
			throw new WWException(res.getStatusText());
		}
	}

	// 본인인증이후,
	// 본인인증 이력등록, 보험료계산 ....
	private String wwToken;
	private String commonToken;
	private JSONObject fn_1_certJson;
	private JSONObject fn_2_premiumJson;
	private JSONObject oagi6002vo;
	private JSONObject fn_3_joinTobeJson;
	private JSONObject fn_4_prodtManualJson;
	private String certConfmSeqNo; //인증순번
	private JSONObject hid;

	public String batch(String caSerial, String caDn,Map<String,Object> ww_info) throws WWException {
		try {
			this.hid = new JSONObject();
			this.fn_1_certJson = ResourceUtil.asJSONObject(tmplCertConfmApi_json);
			this.fn_2_premiumJson = ResourceUtil.asJSONObject(tmplPreminumReqBody_json);
			this.fn_3_joinTobeJson = ResourceUtil.asJSONObject(joinTobe_json);
			this.fn_4_prodtManualJson = ResourceUtil.asJSONObject(prodtManual_json);
			oagi6002vo = fn_2_premiumJson.getJSONObject("oagi6002vo");

			// cert 설정
			fn_1_certJson.put("caSerial", caSerial);
			fn_1_certJson.put("caDn", caDn);
			// premium 설정, TODO: 파라미터로 넘어오도록

			// 0. Token
			wwToken = getWWBearerToken();
			commonToken = getCommonBearerToken();
			// 1. 본인인증이력등록 API 호출
			fn_1_CertConfmApi();
			// 2. 보험료 계산 API 호출
			fn_2_PremiumMathApi();
			// 3. 가입설계 API 호출
			fn_3_JoinTobeApi();
			// 4. 통합청약서(상품설명서) API
			fn_4_ProdtManual();
			// 5. 상품 약관조회 API 호출
			fn_5_ProdtTerms();
			// 6. Data 저장
			fn_6_ApiPremSaveData();
			return "a";
		} catch (Exception e) {
			log.info(e.getMessage());
			throw new WWException("잘못된 JSON 형식입니다.");
		}
	}

	// 1.	CertConfmApi() : 본인인증이력등록 API 호출
	public void fn_1_CertConfmApi() throws WWException {
		fn_1_certJson.put("regNo", oagi6002vo.getString("regNo1") + oagi6002vo.getString("regNo2"));
		fn_1_certJson.put("ptyKorNm", oagi6002vo.getString("ptyKorNm"));

		HttpResponse<JsonNode> res = Unirest
			.post(server + "/v1/OACO0100M01S")
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "210.179.172.177")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(fn_1_certJson.toString())
			.asJson();
		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 본인인증이력등록 API 호출 결과 오류(오류코드):" + resultCode);
			log.info("fn_1_CertConfmApi:" + json.toString());
			this.certConfmSeqNo = json.getString("certConfmSeqNo"); //인증순번
		} else {
			throw new WWException(res.getStatusText());
		}
	}

	// 2.	fnPremiumMathApi(): 보험료 계산 API 호출
	// TODO: token을 파라미터로 받아서 가보험료 계산과 통합할 것.
	public void fn_2_PremiumMathApi() throws WWException {
		HttpResponse<JsonNode> res = Unirest
			.post(server + "/v1/OASF2001M05S")
			.header("Authorization", wwToken)
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "210.179.172.177")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("X-Session-Id", "")
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(fn_2_premiumJson.toString())
			.asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 보험료 요청 결과 오류(오류코드):" + resultCode);
			log.info("fn_2_PremiumMathApi:" + json.toString());

			JSONObject oagi6002vo = json.getJSONObject("oagi6002vo");
			JSONObject giid0100vo = oagi6002vo.getJSONObject("giid0100vo");
			fn_2_premiumJson = json; // 새로 설정.

			hid.put("insStDt", oagi6002vo.getString("insStDt")); //보험개시일자
			hid.put("insEdDt", oagi6002vo.getString("insEdDt")); //보험종료일자
			hid.put("insStTm", oagi6002vo.getString("insStTm")); //보험개시시간
			hid.put("insEdTm", oagi6002vo.getString("insEdTm")); //보험종료시간
			hid.put("prdins", oagi6002vo.getString("prdins")); //보험가입기간

			hid.put("perPrem", giid0100vo.getInt("perPrem")); //본인 부담 보험료
			hid.put("govtPrem", giid0100vo.getInt("govtPrem")); //정부 부담 보험료
			hid.put("lgovtPrem", giid0100vo.getInt("lgovtPrem")); //지자체 부담 보험료
			hid.put("tpymPrem", giid0100vo.getInt("tpymPrem")); //총보험료
			hid.put("executeTime", json.getString("executeTime")); // 세션실행시간
			hid.put("X-Session-Id", res.getHeaders().get("X-Session-Id").get(0)); // 세션ID
			fn_2_premiumJson = json;
		} else {
			throw new WWException(res.getStatusText());
		}
	}

	// 5.	fnJoinTobeApi(): 가입설계 API 호출
	public void fn_3_JoinTobeApi() throws WWException {
		fn_3_joinTobeJson.put("executeTime", fn_2_premiumJson.getString("executeTime"));
		fn_3_joinTobeJson.put("certConfmSeqNo", certConfmSeqNo);

		HttpResponse<JsonNode> res = Unirest
			.post(server + "/v1/OASF2001M08S")
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "210.179.172.177")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("Authorization", wwToken)
			.header("X-Session-Id", (String) hid.get("X-Session-Id"))
			.header("Content-Type", "application/json")
			.body(fn_3_joinTobeJson.toString())
			.asJson();
		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			log.info("fn_3_JoinTobeApi:" + json.toString());
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 가입설계 API 호출 결과 오류(오류코드):" + resultCode);

			fn_2_premiumJson = json; // 새로 설정.
			// TODO: 아래는 필요없음.
			// JSONObject oagi6002vo = json.getJSONObject("oagi6002vo");

			// log.info()
			// hid.put("applNo", oagi6002vo.getString("applNo")); //계약번호
			// hid.put("scNo", oagi6002vo.getString("scNo")); //청약번호
		} else {
			throw new WWException(res.getStatusText());
		}
	}

	// 4.	fnProdtManual(): 통합청약서 (상품설명서) API 호출
	public void fn_4_ProdtManual() throws WWException {
		JSONObject oagi6002vo = fn_2_premiumJson.getJSONObject("oagi6002vo");
		fn_4_prodtManualJson.put("certConfmSeqNo", oagi6002vo.getString("certConfmSeqNo")); // 인증확정순번
		fn_4_prodtManualJson.put("intgAgmtKind", oagi6002vo.getString("agmtKind")); // 가입유형(01:개인, 50:법인)
		fn_4_prodtManualJson.put("regNo", oagi6002vo.getString("regNo")); // 주민번호(가입유형 01시 Setting)
		fn_4_prodtManualJson.put("intgApplNo", oagi6002vo.getString("applNo")); // 계약번호
		fn_4_prodtManualJson.put("ptyKorNm", oagi6002vo.getString("ptyKorNm")); // 고객명
		fn_4_prodtManualJson.put("intgEmailAddr", oagi6002vo.getString("emailAddr")); // 이메일 주소
		fn_4_prodtManualJson.put("intgEmailDomain", oagi6002vo.getString("emailDomain")); // 이메일 도메인

		HttpResponse<JsonNode> res = Unirest
			.post(server + "/v1/OACO0100M07S")
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "210.179.172.177")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json")
			.body(fn_4_prodtManualJson.toString())
			.asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 통합청약서(상품설명서) API 호출 결과 오류(오류코드):" + resultCode);
			log.info("fn_4_ProdtManual:" + json.toString());

			hid.put("intgXmlData", json.getString("intgXmlData")); //RD파일에 사용될 xml 데이터
			hid.put("intgMrdData", json.getString("intgMrdData")); //보고서 파일
			hid.put("intgReportGubun", json.getString("intgReportGubun")); //RD경로데이터 업무구분
			hid.put("intgPageTitle", json.getString("intgPageTitle")); //보고서 제목
		} else {
			throw new WWException(res.getStatusText());
		}
	}

	// 7.	fnProdtTerms(): 상품약관조회 API 연동
	public void fn_5_ProdtTerms() throws WWException {
		JSONObject prodtTerms = new JSONObject();
		prodtTerms.put("resultCode", "");
		prodtTerms.put("resultMsg", "");
		prodtTerms.put("prodCd", "7717"); //상품코드
		prodtTerms.put("flFullUrl", "");
		HttpResponse<JsonNode> res = Unirest
			.post(server + "/v1/OACO0100M08S")
			.header("X-Channel-Id", "Main")
			.header("X-Client-Id", "210.179.172.177")
			.header("X-Menu-Id", "home")
			.header("X-User-Id", "4GE300")
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json")
			.body(prodtTerms.toString())
			.asJson();
		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException("현대해상 상품약관조회 API 호출 결과 오류(오류코드):" + resultCode);

			log.info("hidResultUrl:" + json.getString("flFullUrl"));
		} else {
			throw new WWException(res.getStatusText());
		}
	}

	public void fn_6_ApiPremSaveData() {
		//저장할 정보 담기
		JSONObject oagi6002vo = fn_2_premiumJson.getJSONObject("oagi6002vo");
		JSONObject giid0100vo = oagi6002vo.getJSONObject("giid0100vo");
		String quote_no = "zzz";
		String email = oagi6002vo.getString("emailAddr") + "@" + oagi6002vo.getString("emailDomain");
		String agmtkind = oagi6002vo.getString("agmtKind");
		String bldtotlyrnum = oagi6002vo.getString("bldTotLyrNum");
		String hsarea = oagi6002vo.getString("hsArea");
		String purpose = "?"; //TODO: o,r 이 들어가는데, 사용자로부터 입력받아야함. oagi6002vo.getString("purpose");
		String lsgccd = oagi6002vo.getString("lsgcCd");
		String polestrc = oagi6002vo.getString("poleStrc");
		String roofstrc = oagi6002vo.getString("roofStrc");
		String otwlstrc = oagi6002vo.getString("otwlStrc");
		String objcat = oagi6002vo.getString("objCat");
		String bldfloors1 = oagi6002vo.getString("bldFloors1");
		String bldfloors2 = oagi6002vo.getString("bldFloors2");
		String lobzcd = oagi6002vo.getString("lobzCd");
		String gitdtarifcat1 = oagi6002vo.getString("gitdTarifCat1");
		String objtypcd1 = oagi6002vo.getString("objTypCd1");
		String objtypcd2 = oagi6002vo.getString("objTypCd2");
		String objtypcd3 = oagi6002vo.getString("objTypCd3");
		String elagorgninsdamt1 = oagi6002vo.getString("elagOrgnInsdAmt1");
		String elagorgninsdamt2 = oagi6002vo.getString("elagOrgnInsdAmt2");
		String elagorgninsdamt3 = oagi6002vo.getString("elagOrgnInsdAmt3");
		String ptykornm = oagi6002vo.getString("ptyKorNm");
		String telcat = oagi6002vo.getString("telCat");
		String telno = oagi6002vo.getString("telNo1") + oagi6002vo.getString("telNo2") + oagi6002vo.getString("telNo3");
		String ptybiznm = oagi6002vo.getString("ptyBizNm");
		String bizno = oagi6002vo.getString("bizNo1") + oagi6002vo.getString("bizNo2") + oagi6002vo.getString("bizNo3");
		String objonnaddrcat = oagi6002vo.getString("objOnnaddrCat");
		String objzip = oagi6002vo.getString("objZip1") + oagi6002vo.getString("objZip2");
		String objaddr1 = oagi6002vo.getString("objAddr1");
		String objaddr2 = oagi6002vo.getString("objAddr2");
		String objroadnmcd = oagi6002vo.getString("objRoadNmCd");
		String objtrbdcd = oagi6002vo.getString("objRoadNmCd");
		String objtrbdaddr = oagi6002vo.getString("objRoadNmCd");
		String partnerno = oagi6002vo.getString("partnerNo");
		String tpymprem = giid0100vo.getString("tpymPrem");
		String perprem = giid0100vo.getString("perPrem");
		String govtprem = giid0100vo.getString("govtPrem");
		String lgovtprem = giid0100vo.getString("lgovtPrem");
		String applno = oagi6002vo.getString("applNo");
		String scno = oagi6002vo.getString("scNo");
		wPremcalDetailMapper.insert(
			quote_no,
			email,
			agmtkind,
			bldtotlyrnum,
			hsarea,
			lsgccd,
			polestrc,
			roofstrc,
			otwlstrc,
			objcat,
			bldfloors1,
			bldfloors2,
			lobzcd,
			gitdtarifcat1,
			objtypcd1,
			objtypcd2,
			objtypcd3,
			elagorgninsdamt1,
			elagorgninsdamt2,
			elagorgninsdamt3,
			ptykornm,
			telcat,
			telno,
			ptybiznm,
			bizno,
			objonnaddrcat,
			objzip,
			objaddr1,
			objaddr2,
			objroadnmcd,
			objtrbdcd,
			objtrbdaddr,
			partnerno,
			tpymprem,
			perprem,
			govtprem,
			lgovtprem,
			applno,
			scno,
			purpose
		);
	}
}
