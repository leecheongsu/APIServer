package com.insrb.app.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuAuthExpiredException;
import com.insrb.app.exception.SearchException;
import com.insrb.app.exception.WWException;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.insurance.hi.Hi_1_PrePremium;
import com.insrb.app.insurance.hi.Hi_2_Premium;
import com.insrb.app.insurance.hi.Hi_3_PreventOfDenial;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN005TMapper;
import com.insrb.app.mapper.IN006CMapper;
import com.insrb.app.mapper.IN010TMapper;
import com.insrb.app.mapper.IN101TMapper;
import com.insrb.app.mapper.IN102CMapper;
import com.insrb.app.mapper.IN103CMapper;
import com.insrb.app.util.InsuAuthentication;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.QuoteUtil;
import com.insrb.app.util.ResourceUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/ww")
public class WWController {

	@Autowired
	IN005TMapper in005tMapper;

	@Autowired
	AddressSearch addressSearch;

	// @Autowired
	// HiWindWaterInsurance hi;

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
	IN010TMapper in010tMapper;

	@Autowired
	IN101TMapper in101tMapper;

	@Value("classpath:basic/tmpl_preminum_req_body.json")
	private Resource tmpl_preminum_req_body_json;

	@GetMapping(path = "juso")
	public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
		// Map<String, Object> result = addressSearch.getJusoList(search);
		// return result;

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
				String.valueOf(cover.get("grndFlrCnt")),
				String.valueOf(cover.get("ugrndFlrCnt")),
				String.valueOf(cover.get("etcStrct")),
				cover.toString(),
				"" //풍수해는 전유부가 없다.
			);

			data = in001tMapper.selectById(quote_no);
			data.put("premiums", in102cMapper.selectAll()); //TODO: 김종호, 풍수해에서 이걸 왜 가져오지?
			data.put("lobz_cds", in103cMapper.selectAll());
			Map<String, Object> product = in006cMapper.selectByPcode("h007");
			data.put("product", product);

			// TODO: cover 정보로 template 정보 보완할 것.,
			// WindWaterInsurance.aspx.cs::BuildingInfoText
			Map<String, Object> tmpl = ResourceUtil.asMap(tmpl_preminum_req_body_json);
			Map<String, Object> oagi6002vo = (Map<String, Object>) tmpl.get("oagi6002vo");
			oagi6002vo.put("lsgcCd", data.get("lsgc_cd"));
			oagi6002vo.put("poleStrc", data.get("pole_strc"));
			oagi6002vo.put("roofStrc", data.get("roof_strc"));
			oagi6002vo.put("otwlStrc", data.get("otwl_strc"));

			// WindWaterInsurance.aspx.cs::btn_next_Click
			oagi6002vo.put("objZip1", zip.substring(0, 3));
			oagi6002vo.put("objZip2", zip.substring(3));
			oagi6002vo.put("objAddr1", cover.get("newPlatPlc"));
			oagi6002vo.put("objAddr2", cover.get("bldNm")); // 굳이 번지까지는 필요없지 않을까?
			oagi6002vo.put("objRoadNmCd", String.valueOf(cover.get("naRoadCd")));
			oagi6002vo.put("objTrbdCd", sigungucd + bjdongcd);
			oagi6002vo.put("objTrbdAddr", (String) cover.get("platPlc") + " " + (String) cover.get("bldNm"));

			data.put("ww_info", tmpl);
			return data;
		} catch (SearchException e) {
			log.error("/ww/cover: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, e.getMessage());
		}
	}

	@PostMapping(path = "pre-premium")
	public Map<String, Object> prePremium(@RequestBody(required = true) Map<String, Object> body) {
		log.info("현대해상 가보험료 요청");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		log.info(new JSONObject(data).toString());
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json_str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
			// Map<String, Object> result = hi.getPrePremium(json);
			Map<String, Object> result = Hi_1_PrePremium.GetPrePremium(json_str);
			log.info("Result:{}", result);
			return result;
		} catch (JsonProcessingException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다.");
		} catch (WWException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		}
	}

	@PostMapping(path = "premium")
	public Map<String, Object> premium(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@RequestBody(required = true) Map<String, Object> body
	) {
		String user_id = (String) body.get("user_id");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		log.info("현대해상 실보험료 요청:{}", new JSONObject(data).toString());
		String quote_no = (String) data.get("quote_no"); // hi 안에서 사용함.
		String caSerial = (String) data.get("ca_serial");
		String caDn = (String) data.get("ca_dn");
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
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "현대해상API오류: " + e.getMessage());
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
			String user_id = (String) body.get("user_id");
			String quote_no = (String) body.get("quote_no");
			Map<String, Object> data = (Map<String, Object>) body.get("data");
			log.info("현대해상 부인방지 요청:{}", new JSONObject(data).toString());
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);
			log.info("현대해상 부인방지 요청");
			// return hi.fn_prevent_of_denial(new JSONObject(data));
			String esignurl =  Hi_3_PreventOfDenial.fn_prevent_of_denial(new JSONObject(data));
			in101tMapper.updateEsignurl(quote_no,esignurl);
			return esignurl;
		} catch (WWException e) {
			log.error("/ww/premium: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
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
			String user_id = (String) body.get("user_id");
			Map<String, Object> data = (Map<String, Object>) body.get("data");
			log.info("현대해상 청약확정 요청:{}", new JSONObject(data).toString());
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);
			log.info("현대해상 청약확정 요청");
			// return hi.fn_prevent_of_denial(new JSONObject(data));
			return Hi_3_PreventOfDenial.fn_prevent_of_denial(new JSONObject(data));
		} catch (WWException e) {
			log.error("/ww/premium: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}
}
