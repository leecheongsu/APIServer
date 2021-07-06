package com.insrb.app.api;

import com.insrb.app.exception.*;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.insurance.hi.Hi_1_PrePremium;
import com.insrb.app.insurance.hi.Hi_2_Premium;
import com.insrb.app.insurance.hi.Hi_3_PreventOfDenial;
import com.insrb.app.insurance.hi.Hi_4_Order;
import com.insrb.app.mapper.*;
import com.insrb.app.util.*;
import com.insrb.app.util.cyper.UserInfoCyper;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/ww")
public class WWController {

	@Autowired
	IN005TMapper in005tMapper;

	@Autowired
	AddressSearch addressSearch;

	@Autowired
	Hi_2_Premium hi_2_premium;

	@Autowired
	IN001TMapper in001tMapper;

	@Autowired
	IN102CMapper in102cMapper;

	@Autowired
	IN103CMapper in103cMapper;

	@Autowired
	IN006CMapper in006cMapper;

	@Autowired
	IN003TMapper in003tMapper;

	@Autowired
	IN010TMapper in010tMapper;

	@Autowired
	IN011TMapper in011tMapper;

	@Autowired
	IN101TMapper in101tMapper;

	@Autowired
	KakaoMessageComponent kakaoMessage;

	@Value("classpath:basic/tmpl_preminum_req_body.json")
	private Resource tmpl_preminum_req_body_json;

	@GetMapping(path = "juso")
	public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
		try {
			return addressSearch.getJusoList(search);
		} catch (SearchException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		}
	}

	@GetMapping(path = "cover")
	public Map<String, Object> cover(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji,
		@RequestParam(name = "zip", required = true) String zip
	) {
		try {
			Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
			List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Object> cover = QuoteUtil.GetCoverSummary(items);
			String quote_no = QuoteUtil.GetNewQuoteNo("W");

			in010tMapper.ww_insert(
				quote_no,
				"ETC", //building_type, //풍수해는 building_type이 의미 없다.
				String.valueOf(cover.get("newPlatPlc")),
				"S", // 풍수해는 기본적으로 세대 가입이다.
				String.valueOf(cover.get("bldNm")),
				String.valueOf(cover.get("dong_info")),
				String.valueOf(cover.get("mainPurpsCdNm")),
				String.valueOf(cover.get("newPlatPlc")),
				String.valueOf(cover.get("etcPurps")),
				String.valueOf(cover.get("useAprDay")),
				String.valueOf(cover.get("etcRoof")),
				String.valueOf(cover.get("dongNm")),
				String.valueOf(cover.get("total_area")),
				String.valueOf(cover.get("cnt_sedae")),
				String.valueOf(cover.get("max_grnd_flr_cnt")),
				String.valueOf(cover.get("ugrndFlrCnt")),
				String.valueOf(cover.get("etcStrct")),
				cover.toString(),
				"" //풍수해는 전유부가 없다.
			);

			data = in001tMapper.selectById(quote_no);
			data.put("premiums", in102cMapper.selectAll());
			data.put("lobz_cds", in103cMapper.selectAll());
			Map<String, Object> product = in006cMapper.selectByPcode("h007");
			data.put("product", product);

			// WindWaterInsurance.aspx.cs::BuildingInfoText
			Map<String, Object> tmpl = ResourceUtil.asMap(tmpl_preminum_req_body_json);
			Map<String, Object> oagi6002vo = (Map<String, Object>) tmpl.get("oagi6002vo");
			oagi6002vo.put("bldTotLyrNum", cover.get("max_grnd_flr_cnt")); // 총층수
			oagi6002vo.put("lsgcCd", data.get("lsgc_cd"));
			oagi6002vo.put("poleStrc", data.get("pole_strc"));
			oagi6002vo.put("roofStrc", data.get("roof_strc"));
			oagi6002vo.put("otwlStrc", data.get("otwl_strc"));

			// WindWaterInsurance.aspx.cs::btn_next_Click
			oagi6002vo.put("objZip1", zip.substring(0, 3));
			oagi6002vo.put("objZip2", zip.substring(3));
			oagi6002vo.put("objAddr1", cover.get("newPlatPlc"));
			oagi6002vo.put("objAddr2", InsuNumberUtil.ToIntChar(cover.get("naMainBun")) + ", " + cover.get("bldNm"));
			oagi6002vo.put("objRoadNmCd", String.valueOf(cover.get("naRoadCd")));
			oagi6002vo.put("objTrbdCd", sigungucd + bjdongcd);
			oagi6002vo.put("objTrbdAddr", String.valueOf(cover.get("platPlc")) + " " + String.valueOf(cover.get("bldNm")));

			data.put("ww_info", tmpl);
			return data;
		} catch (SearchException e) {
			log.error("/ww/cover(Search): {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		} catch (DataAccessException e) {
			log.error("ww/cover(DataAccess): {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "공공데이터 수신오류입니다.\n관리자에게 연락해주세요.");
		}
	}

	@PostMapping(path = "pre-premium")
	public Map<String, Object> prePremium(@RequestBody(required = true) Map<String, Object> body) {
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		try {
			JSONObject jsonObj = new JSONObject(data);
			log.debug("현대해상 가보험료 요청:{}", jsonObj.toString());
			Map<String, Object> result = Hi_1_PrePremium.GetPrePremium(jsonObj);
			log.debug("Result:{}", result);
			return result;
		} catch (WWException e) {
			log.error("/ww/pre-premium: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "현대해상 가보험료API오류: " + e.getMessage());
		}
	}

	@PostMapping(path = "premium")
	public Map<String, Object> premium(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@RequestBody(required = true) Map<String, Object> body
	) {
		String user_id = String.valueOf(body.get("user_id"));
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		log.debug("현대해상 실보험료 요청:{}", new JSONObject(data).toString());
		String quote_no = String.valueOf(data.get("quote_no")); // hi 안에서 사용함.
		String caSerial = String.valueOf(data.get("ca_serial"));
		String caDn = String.valueOf(data.get("ca_dn"));
		Map<String, Object> ww_info = (Map<String, Object>) data.get("ww_info");
		if (InsuStringUtil.IsEmpty(user_id)) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user_id");
		if (InsuStringUtil.IsEmpty(quote_no)) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No quote_no");
		if (InsuStringUtil.IsEmpty(caSerial)) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No caSerial");
		if (InsuStringUtil.IsEmpty(caDn)) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No caDn");
		if (ww_info == null || ww_info.size() == 0) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No(Empty) ww_info");

		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);
			hi_2_premium.premium(user_id, data);
			return in101tMapper.selectById(quote_no);
		} catch (WWException e) {
			log.error("/ww/premium: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "현대해상실보험료API오류: " + e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@PostMapping(path = "prevent_denial")
	public String prevent_denail(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@RequestBody(required = true) Map<String, Object> body
	) {
		try {
			String user_id = String.valueOf(body.get("user_id"));
			String quote_no = String.valueOf(body.get("quote_no"));
			String reg_no = String.valueOf(body.get("reg_no")); // 주민번호는 DB 저장하기 뭐해서, 요청시 받는다.
			if (InsuStringUtil.IsEmpty(quote_no)) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No quote_no");
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);

			Map<String, Object> in101t = in101tMapper.selectById(quote_no);
			JSONObject data = new JSONObject();
			data.put("intgAgmtKind", in101t.get("agmtkind"));
			data.put("regNo", reg_no);
			data.put("certConfmSeqNo", in101t.get("certconfmseqno"));
			data.put("mappingNo", in101t.get("mappingno"));
			log.debug("현대해상 부인방지 요청:{}", data.toString());

			String esignurl = Hi_3_PreventOfDenial.Fn_prevent_of_denial(String.valueOf(in101t.get("sessionid")), data);
			in101tMapper.updateEsignurl(quote_no, esignurl);
			return esignurl;
		} catch (WWException e) {
			log.error("/ww/prevent_denial: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "현대해상부인방지API오류: " + e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	// 현대해상 청약확정
	@PostMapping(path = "order")
	public String order(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@RequestBody(required = true) Map<String, Object> body
	) {
		try {
			String user_id = String.valueOf(body.get("user_id"));
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);

			Map<String, Object> data = (Map<String, Object>) body.get("data");
			log.debug("현대해상 청약확정 요청:{}", new JSONObject(data).toString());

			String quote_no = String.valueOf(data.get("quote_no"));
			if (InsuStringUtil.IsEmpty(quote_no)) new ResponseStatusException(HttpStatus.BAD_REQUEST, "No quote_no");
			String prod_code = String.valueOf(data.get("prod_code"));
			String advisor_no = String.valueOf(data.get("advisor_no"));
			Map<String, Object> terms = (Map<String, Object>) data.get("terms");
			Map<String, Object> card = (Map<String, Object>) data.get("card");
			String regno = String.valueOf(card.get("regNo1")) + String.valueOf(card.get("regNo1"));
			String ptype =  String.valueOf(card.get("cardDivide")); //일수불,할부개월수
			String jumin = UserInfoCyper.EncryptJuminb(regno);
			Map<String, Object> in101t = in101tMapper.selectById(quote_no);
			JSONObject order = makeOrder(in101t, card);
			log.debug("order:{}", order);
			JSONObject giid0410vo_json = Hi_4_Order.FnConfirmsubscription(String.valueOf(in101t.get("sessionid")), order);
			in101tMapper.updateContract(quote_no, giid0410vo_json.toString());
			in003tMapper.delete(quote_no);
			in003tMapper.insertFromIn101t(quote_no, prod_code, jumin, advisor_no, ptype);
			insertTerms(quote_no, terms);
			sendA001KakaoMessage(quote_no, in003tMapper.selectByQuoteNo(quote_no));
			return "OK";
		} catch (WWException e) {
			log.error("/ww/order: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "현대해상청약확정API오류: " + e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (ParseException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		} catch (InsuEncryptException e) {
			log.error("/ww/order: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		}
	}

	@Value("classpath:basic/tmpl_order_api.json")
	private Resource tmplOrderApi_json;

	private JSONObject makeOrder(Map<String, Object> in101t, Map<String, Object> card) {
		JSONObject order = ResourceUtil.asJSONObject(tmplOrderApi_json);
		order.put("executeTime", in101t.get("sessionexectime"));
		order.getJSONObject("oagi6002vo").put("agmtKind", in101t.get("agmtkind"));
		order.getJSONObject("oagi6002vo").put("ptyKorNm", in101t.get("ptykornm"));
		order.getJSONObject("oagi6002vo").put("mappingNo", in101t.get("mappingno"));
		order.getJSONObject("oagi6002vo").put("certConfmSeqNo", in101t.get("certconfmseqno"));

		order.getJSONObject("oagi6002vo").put("regNo1", card.get("regNo1"));
		order.getJSONObject("oagi6002vo").put("regNo2", card.get("regNo2"));
		order.getJSONObject("oagi6002vo").put("cardNo1", card.get("cardNo1"));
		order.getJSONObject("oagi6002vo").put("cardNo2", card.get("cardNo2"));
		order.getJSONObject("oagi6002vo").put("cardNo3", card.get("cardNo3"));
		order.getJSONObject("oagi6002vo").put("cardNo4", card.get("cardNo4"));
		order.getJSONObject("oagi6002vo").put("validMonth", card.get("validMonth"));
		order.getJSONObject("oagi6002vo").put("validYear", card.get("validYear"));
		order.getJSONObject("oagi6002vo").put("cardDivide", card.get("cardDivide"));
		return order;
	}

	private void insertTerms(String quote_no, Map<String, Object> data) {
		int termsA_1 = InsuStringUtil.ToIntOrDefault(data.get("termsa_1"), 0);
		int termsA_2 = InsuStringUtil.ToIntOrDefault(data.get("termsa_2"), 0);
		int termsA_3 = InsuStringUtil.ToIntOrDefault(data.get("termsa_3"), 0);
		int termsA_4 = InsuStringUtil.ToIntOrDefault(data.get("termsa_4"), 0);
		int termsA_5 = InsuStringUtil.ToIntOrDefault(data.get("termsa_5"), 0);
		int termsB_1 = InsuStringUtil.ToIntOrDefault(data.get("termsb_1"), 0);
		int termsB_2 = InsuStringUtil.ToIntOrDefault(data.get("termsb_2"), 0);
		int termsB_3 = InsuStringUtil.ToIntOrDefault(data.get("termsb_3"), 0);
		int termsC_1 = InsuStringUtil.ToIntOrDefault(data.get("termsc_1"), 0);
		int termsC_2 = InsuStringUtil.ToIntOrDefault(data.get("termsc_2"), 0);
		int termsC_3 = InsuStringUtil.ToIntOrDefault(data.get("termsc_3"), 0);
		int termsC_4 = InsuStringUtil.ToIntOrDefault(data.get("termsc_4"), 0);
		int termsC_5 = InsuStringUtil.ToIntOrDefault(data.get("termsc_5"), 0);
		int termsD_1 = InsuStringUtil.ToIntOrDefault(data.get("termsd_1"), 0);
		int termsD_2 = InsuStringUtil.ToIntOrDefault(data.get("termsd_2"), 0);
		int termsD_3 = InsuStringUtil.ToIntOrDefault(data.get("termsd_3"), 0);
		int termsE_1 = InsuStringUtil.ToIntOrDefault(data.get("termse_1"), 0);
		int termsE_2 = InsuStringUtil.ToIntOrDefault(data.get("termse_2"), 0);
		int termsE_3 = InsuStringUtil.ToIntOrDefault(data.get("termse_3"), 0);
		int termsF_1 = InsuStringUtil.ToIntOrDefault(data.get("termsf_1"), 0);
		int termsG_1 = InsuStringUtil.ToIntOrDefault(data.get("termsf_1"), 0);

		in011tMapper.delete(quote_no);
		in011tMapper.insert(
			quote_no,
			termsA_1,
			termsA_2,
			termsA_3,
			termsA_4,
			termsA_5,
			termsB_1,
			termsB_2,
			termsB_3,
			termsC_1,
			termsC_2,
			termsC_3,
			termsC_4,
			termsC_5,
			termsD_1,
			termsD_2,
			termsD_3,
			termsE_1,
			termsE_2,
			termsE_3,
			termsF_1,
			termsG_1
		);
	}

	private void sendA001KakaoMessage(String quote_no, Map<String, Object> in003t) throws ParseException, InsuEncryptException {
		String prod_name = String.valueOf(in003t.get("prod_name")); // 인슈로보주택종합보험(메리츠화재)
		String email = String.valueOf(in003t.get("email"));
		String amt_ins = String.valueOf(in003t.get("amt_ins"));
		String premium = String.valueOf(in003t.get("premium"));
		String mobile = String.valueOf(in003t.get("mobile"));
		String encMobile = UserInfoCyper.DecryptMobile(mobile);
		String polholder = String.valueOf(in003t.get("polholder"));
		String insloc = String.valueOf(in003t.get("insloc"));
		String insurant_a = String.valueOf(in003t.get("insurant_a"));
		// Date insdate = InsuDateUtil.ToDate(String.valueOf( data.get("insdate")));
		// Date ins_from = InsuDateUtil.ToDate(String.valueOf( data.get("ins_from")));
		// Date ins_to = InsuDateUtil.ToDate(String.valueOf( data.get("ins_to")));
		Date insdate = (Date) in003t.get("insdate");
		Date ins_from = (Date) in003t.get("ins_from");
		Date ins_to = (Date) in003t.get("ins_to");

		kakaoMessage.A001(
			quote_no,
			encMobile,
			polholder,
			prod_name,
			insurant_a,
			insloc,
			amt_ins,
			premium,
			quote_no,
			InsuDateUtil.ToChar(insdate, "yyyy.MM.dd"),
			InsuDateUtil.ToChar(ins_from, "yyyy.MM.dd"),
			InsuDateUtil.ToChar(ins_from, "yyyy.MM.dd") + " 24:00 ~ " + InsuDateUtil.ToChar(ins_to, "yyyy.MM.dd") + " 24:00",
			email
		);
	}
}
