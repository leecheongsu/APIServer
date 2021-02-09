package com.insrb.app.api;

import com.insrb.app.exception.SearchException;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN010TMapper;
import com.insrb.app.util.InsuJsonUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.QuoteUtil;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/house")
public class HouseController {

	@Autowired
	AddressSearch addressSearch;

	@Autowired
	IN001TMapper in001tMapper;

	@Autowired
	IN002TMapper in002tMapper;

	@Autowired
	IN010TMapper in010tMapper;

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
		try {
			Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
			List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

			// if (items == null || items.size() < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
			Map<String, Object> cover = QuoteUtil.GetCoverSummary(items);

			String building_type = in001tMapper.getBuildingType(
				(String) cover.get("etcPurps"),
				(String) cover.get("mainPurpsCdNm"),
				String.valueOf(items.size()),
				(String) cover.get("max_grnd_flr_cnt"),
				(String) cover.get("total_area")
			);
			// 주택화재 보험은 주택에 대해서만 가입 가능하다.
			if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
			}
			// 16층 이상은 가입할 수 없다.
			if (InsuStringUtil.ToIntOrDefault((String) cover.get("max_grnd_flr_cnt"), 0) > 15) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "15층 이상은 가입할 수 없습니다.");
			}
			// TODO: 3,4등급 가입 불가 로직 구현할 것.

			String quote_no = QuoteUtil.GetNewQuoteNo("q");
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
		} catch (SearchException e) {
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
		Double tot_area = InsuJsonUtil.IntOrDoubleToDouble(cover.get("totArea"));
		String building_type = in001tMapper.getBuildingType(
			(String) cover.get("etcPurps"),
			(String) cover.get("mainPurpsCdNm"),
			"1",
			String.valueOf(cover.get("grndFlrCnt")),
			String.valueOf(tot_area)
		);

		int cnt_sedae = (int) cover.get("hhldCnt");
		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
		}
		// 단독주택은 단체가입으로~~
		if (InsuStringUtil.Equals(building_type, "ILB")) {
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, "단독주택은 단체가입하셔야 합니다.");
		}
		if (cnt_sedae < 1) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "표제부 세대수가 1 이상이어야 합니다.");
		}
		// TODO: 3,4등급 가입 불가 로직 구현할 것. Validation.cs::Check 참고할 것.

		if (detail == null) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "전유부가 있어야합니다.");

		// 단독주택은 전유부만 온다.
		if (!detail.get("exposPubuseGbCdNm").equals("전유")) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "detail.exposPubuseGbCdNm 항목이 \"전유\" 여야합니다.");
		}

		// 개별 세대의 면적으로 치환
		tot_area = InsuJsonUtil.IntOrDoubleToDouble(detail.get("area"));

		if (InsuStringUtil.IsEmpty(building_type)) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
		}
		if (InsuStringUtil.ToIntOrDefault((String) cover.get("grndFlrCnt"), 0) > 15) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "15층 이상은 가입할 수 없습니다.");
		}

		try {
			String quote_no = QuoteUtil.GetNewQuoteNo("q");
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
		try {
			Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
			return QuoteUtil.GetItemFromHouseInfo(search);
		} catch (SearchException e) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
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
		Map<String, Object> search = addressSearch.getHouseDetailInfo(sigungucd, bjdongcd, bun, ji, dongnm, honm);
		List<Map<String, Object>> items = QuoteUtil.getDetailItemFromHouseInfo(search);
		return items;
	}
}
