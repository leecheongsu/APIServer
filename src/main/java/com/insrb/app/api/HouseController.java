package com.insrb.app.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN010TMapper;
import com.insrb.app.util.InsuStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import kong.unirest.json.JSONArray;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/house")
public class HouseController {

	@Autowired
	AddressSearch addressSearch;

	@Autowired
	IN010TMapper in010tMapper;

	@Autowired
	IN001TMapper in001tMapper;

	@Autowired
	IN002TMapper in002tMapper;

	@GetMapping(path = "juso")
	public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
		return addressSearch.getJusoList(search);
	}

	@PostMapping(path = "quotes/danche")
	public Map<String, Object> quote_danche(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
		List<Map<String, Object>> items = getItemFromHouseInfo(search);

		if (items == null || items.size() < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
		Map<String, Object> cover = getCoverSummary(items);

		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		String building_type = in001tMapper.getBuildingType(
			(String) cover.get("etcPurps"),
			(String) cover.get("mainPurpsCdNm"),
			String.valueOf(items.size()),
			(String)cover.get("max_grnd_flr_cnt"),
			(String)cover.get("total_area")
		);
		if (InsuStringUtil.isEmpty(building_type)) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
		}

		try {
			String quote_no = getNewQuoteNo("q");
			in010tMapper.fireinsurance_insert(
				quote_no,
				building_type,
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
				"" //단체가입은 전유부가 없다.
			);

			Map<String, Object> data = in001tMapper.selectById(quote_no);
			List<Map<String, Object>> detail = in002tMapper.selectById(quote_no);
			data.put("premiums", detail);
			return data;
		} catch (Exception e) {
			log.error("/house/quotes/danche: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
	}

	@PostMapping(path = "quotes/sedae")
	public Map<String, Object> sedae(@RequestBody(required = true) Map<String, Object> body) {
		Map<String, Object> cover = (Map<String, Object>) body.get("cover");
		Map<String, Object> detail = (Map<String, Object>) body.get("detail");

		if (cover == null) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "표제부가 있어야합니다.");

		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		// String building_type = in001tMapper.getBuildingType((String) cover.get("etcPurps"), (String) cover.get("mainPurpsCdNm"));
		Double tot_area = intOrDoubleToDouble(cover.get("totArea"));
		String building_type = in001tMapper.getBuildingType(
			(String) cover.get("etcPurps"),
			(String) cover.get("mainPurpsCdNm"),
			"1",
			String.valueOf(cover.get("grndFlrCnt")),
			String.valueOf(tot_area)
		);

		int cnt_sedae = (int)cover.get("hhldCnt");
		if( cnt_sedae < 1) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "표제부 세대수가 1 이상이어야 합니다.");
		// 단독주택(일반주택)이 아닌 경우 전유부가 있어야 한다.
		if(!InsuStringUtil.equals(building_type, "ILB")){
			if (detail == null) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "전유부가 있어야합니다.");

			// 단독주택은 전유부만 온다.
			if (!detail.get("exposPubuseGbCdNm").equals("전유")) throw new ResponseStatusException(
				HttpStatus.NOT_ACCEPTABLE,
				"detail.exposPubuseGbCdNm 항목이 \"전유\" 여야합니다."
			);
			// 개별 세대의 면적으로 치환
			tot_area = intOrDoubleToDouble(detail.get("area"));
		}

		if (InsuStringUtil.isEmpty(building_type)) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
		}

		try {
			String quote_no = getNewQuoteNo("q");
			in010tMapper.fireinsurance_insert(
				quote_no,
				building_type,
				String.valueOf(cover.get("newPlatPlc")),
				"S", //(String)cover.get("group_ins"),
				String.valueOf(cover.get("bldNm")),
				String.valueOf(cover.get("bldNm")), //dong_info
				String.valueOf(cover.get("mainPurpsCdNm")),
				String.valueOf(cover.get("newPlatPlc")),
				String.valueOf(cover.get("etcPurps")),
				String.valueOf(cover.get("useAprDay")),
				String.valueOf(cover.get("etcRoof")),
				String.valueOf(cover.get("dongNm")),
				String.valueOf(tot_area),
				"1", //String.valueOf(cover.get("cnt_sedae")),
				String.valueOf(cover.get("grndFlrCnt")),
				String.valueOf(cover.get("ugrndFlrCnt")),
				String.valueOf(cover.get("etcStrct")),
				cover.toString(),
				detail.toString()
			);

			Map<String, Object> data = in001tMapper.selectById(quote_no);
			List<Map<String, Object>> in002t = in002tMapper.selectById(quote_no);
			data.put("premiums", in002t);
			return data;
		} catch (Exception e) {
			log.error("/house/quotes/sedae: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
	}

	@GetMapping(path = "cover")
	public List<Map<String, Object>> cover(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
		return getItemFromHouseInfo(search);
	}

	@GetMapping(path = "detail")
	public List<Map<String, Object>> detail(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji,
		@RequestParam(name = "dongnm", required = true) String dongnm,
		@RequestParam(name = "honm", required = false) String honm
	) {
		Map<String, Object> search = addressSearch.getHouseDetailInfo(sigungucd, bjdongcd, bun, ji, dongnm,honm);
		List<Map<String, Object>> items = getDetailItemFromHouseInfo(search);
		return items;
	}

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


	private List<Map<String, Object>> getDetailItemFromHouseInfo(Map<String, Object> search) throws ResponseStatusException {
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
			List<Map<String, Object>> list = (List<Map<String, Object>>) items.get("item");
			for(Map<String, Object> row : list){
				if(InsuStringUtil.equals((String)row.get("exposPubuseGbCdNm"), "전유"))
				item.add(row);
			}
		}

		return item;
	}

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

			if(item.get("grndFlrCnt")!=null){
				int grnd_flr_cnt =  (int) item.get("grndFlrCnt");
				max_grnd_flr_cnt = (grnd_flr_cnt > max_grnd_flr_cnt) ? grnd_flr_cnt : max_grnd_flr_cnt;
			}
			// 마지막 데이터의 승인일.
			useAprDay =  item.get("useAprDay");
		}
		// if (cover == null) cover = items.get(items.size() -1 );
		cover = items.get(items.size() -1 );

		
		cover.put("cnt_sedae", String.valueOf(cnt_sedae));
		cover.put("total_area", String.valueOf(total_area));
		cover.put("dong_info", new JSONArray().put(dong_info).toString());
		cover.put("max_grnd_flr_cnt", String.valueOf(max_grnd_flr_cnt));
		cover.put("useAprDay",useAprDay);
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
