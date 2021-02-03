package com.insrb.app.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insrb.app.exception.WWException;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.insurance.hi.HiWindWaterInsurance;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN007CMapper;
import com.insrb.app.mapper.IN010TMapper;
import com.insrb.app.util.ResourceUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kong.unirest.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	AddressSearch addressSearch;

	@Value("classpath:basic/tmpl_preminum_req_body.json")
	private Resource tmpl_preminum_req_body_json;

	@Autowired
	private HiWindWaterInsurance hi;

	@Autowired
	IN001TMapper in001tMapper;

	@Autowired
	IN010TMapper in010tMapper;

	@Autowired
	IN007CMapper in007cMapper;

	@GetMapping(path = "juso")
	public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
		Map<String, Object> result = addressSearch.getJusoList(search);
		return result;
	}

	@GetMapping(path = "cover")
	public Map<String, Object> cover(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
		List<Map<String, Object>> items = getItemFromHouseInfo(search);

		if (items == null || items.size() < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> cover = getCoverSummary(items);

		try {
			String quote_no = getNewQuoteNo("w");
			in010tMapper.fireinsurance_insert(
				quote_no,
				"DSD", //building_type, TODO 풍수해용 BuildingType가져오는 함수만들것.
				String.valueOf(cover.get("newPlatPlc")),
				"T", //(String)cover.get("group_ins"),
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
			data.put("code", in007cMapper.selectAll());
			// TODO: cover 정보로 template 정보 보완할 것.,
			Map<String,Object> tmpl = ResourceUtil.asMap(tmpl_preminum_req_body_json);
			tmpl.put("poleStrc","");
			tmpl.put("roofStrc","");
			tmpl.put("otwlStrc","");
			// .....TODO:
			data.put("template", tmpl);
			return data;
		} catch (Exception e) {
			log.error("/house/quotes/danche: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
	}

	@PostMapping(path = "pre-premium")
	public Map<String, Object> prePremium(@RequestBody(required = true) Map<String, Object> body) {
		log.info("현대해상 가보험료 요청");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
			Map<String, Object> result = hi.getPrePremium(json);
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

	@GetMapping(path = "batch")
	public String batch(
		@RequestParam(name = "caSerial", required = true) String caSerial,
		@RequestParam(name = "caDn", required = true) String caDn
	)
		throws WWException {
		log.info("현대해상 Batch 요청");
		return hi.batch(caSerial, caDn);
	}

	// TODO: HouseController에 있는 것 복사함.

	private List<Map<String, Object>> getItemFromHouseInfo(Map<String, Object> search) throws ResponseStatusException {
		Map<String, Object> response = (Map<String, Object>) search.get("response");
		Map<String, Object> header = (Map<String, Object>) response.get("header");

		if (!"00".equals(header.get("resultCode"))) throw new ResponseStatusException(HttpStatus.NO_CONTENT);

		Map<String, Object> body = (Map<String, Object>) response.get("body");
		Map<String, Object> items = (Map<String, Object>) body.get("items");

		List<Map<String, Object>> item = new ArrayList<Map<String, Object>>();

		// 단건인 경우, XML 파서가 단건인 경우 배열 처리 안하고 넘기는 것 같음.
		if (items.get("item") instanceof HashMap) {
			item.add((Map<String, Object>) items.get("item"));
		} else { //리스트로 올 경우
			item = (List<Map<String, Object>>) items.get("item");
		}

		return item;
	}

	// private List<Map<String, Object>> getDetailItemFromHouseInfo(Map<String, Object> search) throws ResponseStatusException {
	// 	Map<String, Object> response = (Map<String, Object>) search.get("response");
	// 	Map<String, Object> header = (Map<String, Object>) response.get("header");

	// 	if (!"00".equals(header.get("resultCode"))) throw new ResponseStatusException(HttpStatus.NO_CONTENT);

	// 	Map<String, Object> body = (Map<String, Object>) response.get("body");
	// 	Map<String, Object> items = (Map<String, Object>) body.get("items");

	// 	List<Map<String, Object>> item = new ArrayList<Map<String, Object>>();

	// 	// 단건인 경우, XML 파서가 단건인 경우 배열 처리 안하고 넘기는 것 같음.
	// 	if (items.get("item") instanceof HashMap) {
	// 		item.add((Map<String, Object>) items.get("item"));
	// 	} else { //리스트로 올 경우
	// 		List<Map<String, Object>> list = (List<Map<String, Object>>) items.get("item");
	// 		for(Map<String, Object> row : list){
	// 			if(InsuStringUtil.equals((String)row.get("exposPubuseGbCdNm"), "전유"))
	// 			item.add(row);
	// 		}
	// 	}

	// 	return item;
	// }

	private Map<String, Object> getCoverSummary(List<Map<String, Object>> items) {
		int cnt_sedae = 0;
		double total_area = 0.00;
		Object useAprDay = "";
		Map<String, Object> dong_info = new HashMap<String, Object>();

		Map<String, Object> cover = null;
		int max_grnd_flr_cnt = 1;
		for (Map<String, Object> item : items) {
			int sedae = (int) item.get("hhldCnt");
			// if (cover == null && sedae > 0) cover = item; // 세대가 한세대라도 있는 건물(동)을 대표로 한다.
			cnt_sedae += sedae;
			total_area += intOrDoubleToDouble(item.get("totArea"));
			String dong_name = item.get("dongNm") != "" ? (String) item.get("dongNm") : (String) item.get("bldNm");
			dong_info.put(dong_name, item.get("totArea"));

			if (item.get("grndFlrCnt") != null) {
				int grnd_flr_cnt = (int) item.get("grndFlrCnt");
				max_grnd_flr_cnt = (grnd_flr_cnt > max_grnd_flr_cnt) ? grnd_flr_cnt : max_grnd_flr_cnt;
			}
			// 마지막 데이터의 승인일.
			useAprDay = item.get("useAprDay");
		}
		// if (cover == null) cover = items.get(items.size() -1 );
		cover = items.get(items.size() - 1);

		cover.put("cnt_sedae", String.valueOf(cnt_sedae));
		cover.put("total_area", String.valueOf(total_area));
		cover.put("dong_info", new JSONArray().put(dong_info).toString());
		cover.put("max_grnd_flr_cnt", String.valueOf(max_grnd_flr_cnt));
		cover.put("useAprDay", useAprDay);
		return cover;
	}

	private double intOrDoubleToDouble(Object item) {
		if (item instanceof Integer) {
			return (int) item;
		} else if (item instanceof Double) {
			return (double) item;
		}
		return 0.0;
	}

	private String getNewQuoteNo(String prefix) {
		// DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		// DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		// 밀리세컨드까지 해야 UK 오류가 나지 않는다.
		// 더 확실한 방법은 SEQ를 쓰는 것이다.
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		Date date = new Date();
		return prefix + dateFormat.format(date);
	}
}
