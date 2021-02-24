package com.insrb.app.insurance.hi;

import com.insrb.app.exception.InsuEncryptException;
import com.insrb.app.exception.WWException;
import com.insrb.app.mapper.IN101TMapper;
import com.insrb.app.util.InsuJsonUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.ResourceUtil;
import com.insrb.app.util.cyper.UserInfoCyper;
import java.util.Map;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

//ref : http://kong.github.io/unirest-java/#requests

@Slf4j
@Component
public class Hi_2_Premium {

	@Value("classpath:basic/tmpl_cert_confm_api.json")
	private Resource tmplCertConfmApi_json;

	@Value("classpath:basic/tmpl_join_tobe.json")
	private Resource tmplJoinTobe_json;

	@Value("classpath:basic/tmpl_prodt_manual.json")
	private Resource tmplProdtManual_json;

	@Autowired
	IN101TMapper in101tMapper;

	// 본인인증이후,
	// 본인인증 이력등록, 보험료계산 ....
	private String wwToken;
	private String commonToken;
	private String quote_no;
	private String[] user_email;
	private JSONObject fn_1_certJson;
	private JSONObject fn_2_premiumJson;
	private JSONObject oagi6002vo;
	private JSONObject fn_3_joinTobeJson;
	private JSONObject fn_4_prodtManualJson;
	private String certConfmSeqNo; //인증순번
	// private JSONObject hid; // TODO: 이거 왜 저장하지?

	private String X_Session_Id = ""; //세션 ID
	private String X_Session_Execute_Time = ""; // 세션 실행 시간
	private String localurltmp = ""; // 본인 전자 서명 URL
	private String mappingno = ""; // 매핑 번호

	public boolean premium(String user_id, Map<String, Object> data) throws WWException {
		try {
			// String user_id = (String) data.get("user_id");
			user_email = user_id.split("@");
			String caSerial = (String) data.get("ca_serial");
			String caDn = (String) data.get("ca_dn");
			quote_no = (String) data.get("quote_no");

			// this.hid = new JSONObject();
			JSONObject ww_json = new JSONObject(data.get("ww_info"));
			oagi6002vo = ww_json.getJSONObject("oagi6002vo");
			this.fn_1_certJson = ResourceUtil.asJSONObject(tmplCertConfmApi_json);
			this.fn_2_premiumJson = ww_json; // ResourceUtil.asJSONObject(tmplPreminumReqBody_json);
			this.fn_3_joinTobeJson = ResourceUtil.asJSONObject(tmplJoinTobe_json);
			this.fn_4_prodtManualJson = ResourceUtil.asJSONObject(tmplProdtManual_json);

			// OkCert로부터 넘어온 본인 인증 정보
			fn_1_certJson.put("caSerial", caSerial);
			fn_1_certJson.put("caDn", caDn);

			// 0. Token
			wwToken = HiToken.GetWWBearerToken();
			commonToken = HiToken.GetCommonBearerToken();
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
			// 6. 전자 서명할수 있는 URL 요청
			fn_6_ApifnElectronicSignApi();
			// 7. Data 저장
			fn_7_ApiPremSaveData();

			// 8. 모든 작업을 완료하고 전자서명/부인방지/카드결제에 필요한 정보를 내려보낸다. --> 모든 정보를 IN101T에 저장.
			// Map<String,Object> rtn = new HashMap<String,Object>();
			// rtn.put("localurltmp", localurltmp);
			// rtn.put("mappingno", mappingno);
			// rtn.put("certconfmseqno", certConfmSeqNo);
			// rtn.put("executetime", X_Session_Execute_Time);
			// return rtn;
			return true;
		} catch (Exception e) {
			log.debug(e.getMessage());
			throw new WWException("현대해상 청약오류:" + e.getMessage());
		}
	}

	// 1.	CertConfmApi() : 본인인증이력등록 API 호출
	private void fn_1_CertConfmApi() throws WWException {
		fn_1_certJson.put("regNo", oagi6002vo.getString("regNo1") + oagi6002vo.getString("regNo2"));
		fn_1_certJson.put("ptyKorNm", oagi6002vo.getString("ptyKorNm"));

		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OACO0100M01S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(fn_1_certJson.toString())
			.asJson();
		JSONObject json = res.getBody().getObject();
		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");
			log.debug("fn_1_CertConfmApi:" + json.toString());
			this.certConfmSeqNo = json.getString("certConfmSeqNo"); //인증순번
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}

	// 2.	fnPremiumMathApi(): 보험료 계산 API 호출
	// TODO: token을 파라미터로 받아서 가보험료 계산과 통합할 것.
	private void fn_2_PremiumMathApi() throws WWException {
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OASF2001M05S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", wwToken)
			.header("X-Session-Id", "")
			.header("Content-Type", "application/json;charset=UTF-8")
			.body(fn_2_premiumJson.toString())
			.asJson();

		JSONObject json = res.getBody().getObject();
		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");
			log.debug("fn_2_PremiumMathApi:" + json.toString());

			// JSONObject oagi6002vo = json.getJSONObject("oagi6002vo");
			// JSONObject giid0100vo = oagi6002vo.getJSONObject("giid0100vo");

			// hid.put("insStDt", oagi6002vo.getString("insStDt")); //보험개시일자
			// hid.put("insEdDt", oagi6002vo.getString("insEdDt")); //보험종료일자
			// hid.put("insStTm", oagi6002vo.getString("insStTm")); //보험개시시간
			// hid.put("insEdTm", oagi6002vo.getString("insEdTm")); //보험종료시간
			// hid.put("prdins", oagi6002vo.getString("prdins")); //보험가입기간

			// hid.put("perPrem", giid0100vo.getInt("perPrem")); //본인 부담 보험료
			// hid.put("govtPrem", giid0100vo.getInt("govtPrem")); //정부 부담 보험료
			// hid.put("lgovtPrem", giid0100vo.getInt("lgovtPrem")); //지자체 부담 보험료
			// hid.put("tpymPrem", giid0100vo.getInt("tpymPrem")); //총보험료
			// hid.put("executeTime", json.getString("executeTime")); // 세션실행시간
			// hid.put("X-Session-Id", res.getHeaders().get("X-Session-Id").get(0)); // 세션ID

			X_Session_Execute_Time = json.getString("executeTime"); // 세션실행시간:세션키 05S에서 받은 걸 08S에서 사용
			X_Session_Id = res.getHeaders().get("X-Session-Id").get(0); // 세션ID

			fn_2_premiumJson = json;
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}

	// 5.	fnJoinTobeApi(): 가입설계 API 호출
	private void fn_3_JoinTobeApi() throws WWException {
		fn_3_joinTobeJson.put("executeTime", X_Session_Execute_Time);
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("ptyKorNm", oagi6002vo.getString("ptyKorNm"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("regNo1", oagi6002vo.getString("regNo1"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("regNo2", oagi6002vo.getString("regNo2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("emailAddr", user_email[0]);
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("emailDomain", user_email[1]);
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("zip1", oagi6002vo.getString("objZip1"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("zip2", oagi6002vo.getString("objZip2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("addr1", oagi6002vo.getString("objZip2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("addr2", oagi6002vo.getString("objZip2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("roadNmCd", oagi6002vo.getString("objZip2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("trbdCd", oagi6002vo.getString("objZip2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("trbdAddr", oagi6002vo.getString("objZip2"));
		fn_3_joinTobeJson.getJSONObject("oagi6002vo").put("certConfmSeqNo", this.certConfmSeqNo);
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OASF2001M08S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", wwToken)
			.header("X-Session-Id", X_Session_Id)
			.header("Content-Type", "application/json")
			.body(fn_3_joinTobeJson.toString())
			.asJson();
		JSONObject json = res.getBody().getObject();
		if (res.getStatus() == 200) {
			log.debug("fn_3_JoinTobeApi:" + json.toString());
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");

			fn_2_premiumJson = json; // 새로 설정.
			// TODO: 아래는 필요없음.
			// JSONObject oagi6002vo = json.getJSONObject("oagi6002vo");

			// log.debug()
			// hid.put("applNo", oagi6002vo.getString("applNo")); //계약번호
			// hid.put("scNo", oagi6002vo.getString("scNo")); //청약번호
			X_Session_Id = res.getHeaders().get("X-Session-Id").get(0); // 세션ID
			X_Session_Execute_Time = json.getString("executeTime"); // 세션실행시간:세션키 08S에서 받은 걸 12S에서 사용
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}

	// 4.	fnProdtManual(): 통합청약서 (상품설명서) API 호출
	private void fn_4_ProdtManual() throws WWException {
		JSONObject oagi6002vo = fn_2_premiumJson.getJSONObject("oagi6002vo");
		fn_4_prodtManualJson.put("certConfmSeqNo", oagi6002vo.getString("certConfmSeqNo")); // 인증확정순번
		fn_4_prodtManualJson.put("intgAgmtKind", oagi6002vo.getString("agmtKind")); // 가입유형(01:개인, 50:법인)
		fn_4_prodtManualJson.put("regNo", oagi6002vo.getString("regNo")); // 주민번호(가입유형 01시 Setting)
		fn_4_prodtManualJson.put("intgApplNo", oagi6002vo.getString("applNo")); // 계약번호
		fn_4_prodtManualJson.put("ptyKorNm", oagi6002vo.getString("ptyKorNm")); // 고객명
		fn_4_prodtManualJson.put("intgEmailAddr", oagi6002vo.getString("emailAddr")); // 이메일 주소
		fn_4_prodtManualJson.put("intgEmailDomain", oagi6002vo.getString("emailDomain")); // 이메일 도메인

		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OACO0100M07S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			// .header("X-Session-Id", X_Session_Id)
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json")
			.body(fn_4_prodtManualJson.toString())
			.asJson();

		JSONObject json = res.getBody().getObject();
		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");
			log.debug("fn_4_ProdtManual:" + json.toString());
			// hid.put("intgXmlData", json.getString("intgXmlData")); //RD파일에 사용될 xml 데이터
			// hid.put("intgMrdData", json.getString("intgMrdData")); //보고서 파일
			// hid.put("intgReportGubun", json.getString("intgReportGubun")); //RD경로데이터 업무구분
			// hid.put("intgPageTitle", json.getString("intgPageTitle")); //보고서 제목
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}

	// 5.	fnProdtTerms(): 상품약관조회 API 연동
	private void fn_5_ProdtTerms() throws WWException {
		JSONObject prodtTerms = new JSONObject();
		prodtTerms.put("resultCode", "");
		prodtTerms.put("resultMsg", "");
		prodtTerms.put("prodCd", "7717"); //상품코드
		prodtTerms.put("flFullUrl", "");
		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OACO0100M08S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			// .header("X-Session-Id", X_Session_Id)
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json")
			.body(prodtTerms.toString())
			.asJson();
		JSONObject json = res.getBody().getObject();
		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");

			log.debug("hidResultUrl:" + json.getString("flFullUrl"));
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}

	private void fn_6_ApifnElectronicSignApi() throws WWException {
		JSONObject oagi6002vo = fn_2_premiumJson.getJSONObject("oagi6002vo");
		JSONObject giid0100vo = oagi6002vo.getJSONObject("giid0100vo");

		JSONObject eSign = new JSONObject();
		eSign.put("certConfmSeqNo", oagi6002vo.getString("certConfmSeqNo")); // 인증확정순번
		eSign.put("intgAgmtKind", oagi6002vo.getString("agmtKind")); // 가입유형(01:개인, 50:법인)계
		eSign.put("regNo", oagi6002vo.getString("regNo")); // 주민번호 - 운영계
		eSign.put("oaKorNm", oagi6002vo.getString("ptyKorNm")); // 성명 - 운영계
		eSign.put("oaInsureKorNm", oagi6002vo.getString("ptyKorNm")); // 피보험자명 - 운영계
		eSign.put("oaGender", oagi6002vo.getString("regNo2").substring(0, 1)); // 성별 - 운영계
		eSign.put("oaTotPremAmt", giid0100vo.getString("perPrem")); // 본인부담보험료
		eSign.put("oaFirstAmt", giid0100vo.getString("perPrem")); // 조회부험료 - 본인부담보험료
		eSign.put("agmtNo", oagi6002vo.getString("applNo")); // 계약번호
		eSign.put("cfrmIp", "210.179.172.177"); // 신청자IP정보
		eSign.put("bzDetCd", "10061022"); // 업무상세코드

		HttpResponse<JsonNode> res = Unirest
			.post(HiConfig.SERVER + "/v1/OACO0100M05S")
			.header("X-Channel-Id", HiConfig.X_Channel_Id)
			.header("X-Client-Id", HiConfig.X_Client_Id)
			.header("X-Menu-Id", HiConfig.X_Menu_Id)
			.header("X-User-Id", HiConfig.X_User_Id)
			.header("Authorization", commonToken)
			.header("Content-Type", "application/json")
			.body(eSign.toString())
			.asJson();

		JSONObject json = res.getBody().getObject();
		if (res.getStatus() == 200) {
			int resultCode = json.getInt("resultCode");
			if (resultCode != 0) throw new WWException(json.getString("resultCode") + "(" + json.getString("resultMsg") + ")");
			log.debug("fn_7_ApifnElectronicSignApi(localUrlTmp):" + json.getString("localUrlTmp"));
			log.debug("fn_7_ApifnElectronicSignApi(mappingNo):" + json.getString("mappingNo"));
			localurltmp = json.getString("localUrlTmp");
			mappingno = json.getString("mappingNo");
		} else {
			throw new WWException(json.getString("message") + "(" + json.getString("code") + ")");
		}
	}

	private void fn_7_ApiPremSaveData() throws InsuEncryptException {
		//저장할 정보 담기
		JSONObject oagi6002vo = fn_2_premiumJson.getJSONObject("oagi6002vo");
		JSONObject giid0100vo = oagi6002vo.getJSONObject("giid0100vo");
		String email = oagi6002vo.getString("emailAddr") + "@" + oagi6002vo.getString("emailDomain");
		String agmtkind = oagi6002vo.getString("agmtKind");
		String bldtotlyrnum = oagi6002vo.getString("bldTotLyrNum");
		String hsarea = oagi6002vo.getString("hsArea");
		String lsgccd = oagi6002vo.getString("lsgcCd");
		String polestrc = oagi6002vo.getString("poleStrc");
		String roofstrc = oagi6002vo.getString("roofStrc");
		String otwlstrc = oagi6002vo.getString("otwlStrc");
		String objcat = oagi6002vo.getString("objCat");
		String bldfloors1 = oagi6002vo.getString("bldFloors1");
		String bldfloors2 = oagi6002vo.getString("bldFloors2");
		String lobzcd = oagi6002vo.getString("lobzCd");
		String gitdtarifcat1 = oagi6002vo.getString("gitdTarifCat1");
		// String objtypcd1 = oagi6002vo.getString("objTypCd1");
		// String objtypcd2 = oagi6002vo.getString("objTypCd2");
		String objtypcd1 = InsuJsonUtil.IfNullDefault(oagi6002vo, "objTypCd1", "");
		String objtypcd2 = InsuJsonUtil.IfNullDefault(oagi6002vo, "objTypCd2", "");
		String objtypcd3 = InsuJsonUtil.IfNullDefault(oagi6002vo, "objTypCd3", "");
		String purpose = "?"; // 건물담보가 있으면 소유, 아니면 렌트
		purpose = (InsuStringUtil.Equals(objtypcd1, "Y")) ? "o" : "r";
		// String elagorgninsdamt1 = oagi6002vo.getString("elagOrgnInsdAmt1");
		// String elagorgninsdamt2 = oagi6002vo.getString("elagOrgnInsdAmt2");
		// String elagorgninsdamt3 = (String)oagi6002vo.get("elagOrgnInsdAmt3");
		String elagorgninsdamt1 = InsuJsonUtil.IfNullDefault(oagi6002vo, "elagOrgnInsdAmt1", "");
		String elagorgninsdamt2 = InsuJsonUtil.IfNullDefault(oagi6002vo, "elagOrgnInsdAmt2", "");
		String elagorgninsdamt3 = InsuJsonUtil.IfNullDefault(oagi6002vo, "elagOrgnInsdAmt3", "");
		String ptykornm = oagi6002vo.getString("ptyKorNm");
		String telcat = oagi6002vo.getString("telCat");
		String telno = oagi6002vo.getString("telNo1") + oagi6002vo.getString("telNo2") + oagi6002vo.getString("telNo3");
		telno = UserInfoCyper.EncryptMobile(telno);
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
		String insstdt = oagi6002vo.getString("insStDt"); //보험개시일자
		String inseddt = oagi6002vo.getString("insEdDt"); //보험종료일자
		String inssttm = oagi6002vo.getString("insStTm"); //보험개시시간
		String insedtm = oagi6002vo.getString("insEdTm"); //보험종료시간
		String prdins = oagi6002vo.getString("prdins"); //보험가입기간
		String tpymprem = giid0100vo.getString("tpymPrem"); //총보험료
		String perprem = giid0100vo.getString("perPrem"); //본인 부담 보험료
		String govtprem = giid0100vo.getString("govtPrem"); //정부 부담 보험료
		String lgovtprem = giid0100vo.getString("lgovtPrem"); //지자체 부담 보험료
		String applno = oagi6002vo.getString("applNo");
		String scno = oagi6002vo.getString("scNo");

		in101tMapper.delete(quote_no);
		in101tMapper.insert(
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
			insstdt,
			inseddt,
			inssttm,
			insedtm,
			prdins,
			tpymprem,
			perprem,
			govtprem,
			lgovtprem,
			applno,
			scno,
			purpose,
			localurltmp,
			mappingno,
			X_Session_Execute_Time,
			X_Session_Id,
			certConfmSeqNo
		);
	}
}
