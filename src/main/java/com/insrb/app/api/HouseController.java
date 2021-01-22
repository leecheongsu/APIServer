package com.insrb.app.api;

import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN010TMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
		if (!String.valueOf(cover.get("etcPurps")).contains("주택")) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"주택만 가입 가능합니다.");

		try {
			String quote_no = getNewQuoteNo("q");
			in010tMapper.insert(
				quote_no,
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
				""
			);

			Map<String, Object> data = in001tMapper.selectById(quote_no);
			List<Map<String, Object>> detail = in002tMapper.selectById(quote_no);
			data.put("premiums", detail);
			return data;
		} catch (Exception e) {
			log.error("/house/quotes/danche: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,e.getMessage());
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
	public Map<String, Object> detail(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> search_cover = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
		Map<String, Object> search_detail = addressSearch.getHouseDetailInfo(sigungucd, bjdongcd, bun, ji);
		data.put("cover", getItemFromHouseInfo(search_cover));
		data.put("detail", getItemFromHouseInfo(search_detail));
		return data;
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

	private Map<String, Object> getCoverSummary(List<Map<String, Object>> items) {
		int cnt_sedae = 0;
		double total_area = 0.00;
		Map<String, Object> dong_info = new HashMap<String, Object>();

		Map<String, Object> cover = items.get(0);
		for (Map<String, Object> item : items) {
			cnt_sedae += (int) item.get("hhldCnt");
			total_area += intOrDoubleToDouble(item.get("totArea"));
			String dong_name = item.get("dongNm") != "" ? (String) item.get("dongNm") : (String) item.get("bldNm");
			dong_info.put(dong_name, item.get("totArea"));
		}
		cover.put("cnt_sedae", String.valueOf(cnt_sedae));
		cover.put("total_area", String.valueOf(total_area));
		cover.put("dong_info", new JSONArray().put(dong_info).toString());
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
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		return prefix + dateFormat.format(date);
	}
}
