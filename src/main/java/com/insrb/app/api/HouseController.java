package com.insrb.app.api;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.insrb.app.exception.SearchException;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN006CMapper;
import com.insrb.app.mapper.IN010TMapper;
import com.insrb.app.util.InsuJsonUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.QuoteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/house")
public class HouseController {

	@Autowired
	AddressSearch addressSearch;

	@Autowired
	IN006CMapper in006cMapper;

	@Autowired
	IN001TMapper in001tMapper;

	@Autowired
	IN002TMapper in002tMapper;

	@Autowired
	IN010TMapper in010tMapper;

	@GetMapping(path = "juso")
	public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
		try {
			return addressSearch.getJusoList(search);
		} catch (SearchException e) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		}
	}

	@PostMapping(path = "quotes/danche")
	public Map<String, Object> quote_danche(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		log.debug("quotes/danche:{},{},{},{}", sigungucd, bjdongcd, bun, ji);
		try {
			Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
			List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

			// if (items == null || items.size() < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
			Map<String, Object> cover = QuoteUtil.GetCoverSummary(items);

			String building_type = in001tMapper.getBuildingType(
				String.valueOf(cover.get("etcPurps")),
				String.valueOf(cover.get("mainPurpsCdNm")),
				String.valueOf(items.size()),
				String.valueOf(cover.get("max_grnd_flr_cnt")),
				String.valueOf(cover.get("total_area"))
			);
			// 주택화재 보험은 주택에 대해서만 가입 가능하다.
			if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
			}
			// 16층 이상 건물은 가입할 수 없다.
			if (InsuStringUtil.ToIntOrDefault(cover.get("max_grnd_flr_cnt"), 0) > 15) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "16층 이상 건물은 가입할 수 없습니다.");
			}

			String quote_no = QuoteUtil.GetNewQuoteNo("Q");
			in010tMapper.fireinsurance_insert(
				quote_no,
				building_type,
				getNotEmptyAddress(cover.get("platPlc"), cover.get("newPlatPlc")),
				// String.valueOf(cover.get("newPlatPlc")),
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

			// 단독주택(단독주택,다가구주택) 중 5억 이상은 가입 불가
			Map<String, Object> data = in001tMapper.selectById(quote_no);
			if (InsuStringUtil.Equals(building_type, "ILB") || InsuStringUtil.Equals(building_type, "DGG")) {
				if (InsuStringUtil.ToIntOrDefault(data.get("amt_ins"), 0) > 500000000) {
					throw new ResponseStatusException(
						HttpStatus.NOT_ACCEPTABLE,
						"보험가입금액 5억 이상 단독/다가구주택은 가입할 수 없습니다."
					);
				}
			}

			List<Map<String, Object>> detail = in002tMapper.selectById(quote_no);
			data.put("premiums", detail);
			Map<String, Object> product = in006cMapper.selectByPcode("m002");
			data.put("product", product);
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
		if (detail == null) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "전유부가 있어야합니다.");

		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		Double tot_area = InsuJsonUtil.IntOrDoubleToDouble(cover.get("totArea"));
		String building_type = in001tMapper.getBuildingType(
			String.valueOf(cover.get("etcPurps")),
			String.valueOf(cover.get("mainPurpsCdNm")),
			"1",
			String.valueOf(cover.get("grndFlrCnt")),
			String.valueOf(tot_area)
		);

		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
		}
		// 단독주택은 단체가입으로~~
		if (InsuStringUtil.Equals(building_type, "ILB")) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "단독주택은 단체가입하셔야 합니다.");
		}
		int cnt_sedae = (int) cover.get("hhldCnt");
		if (cnt_sedae < 1) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "표제부 세대수가 1 이상이어야 합니다.");
		}

		// 개별 세대의 면적으로 치환
		tot_area = InsuJsonUtil.IntOrDoubleToDouble(detail.get("area"));

		// if (InsuStringUtil.IsEmpty(building_type)) {
		// 	throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
		// }
		if (InsuStringUtil.ToIntOrDefault(cover.get("grndFlrCnt"), 0) > 15) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "16층 이상 건물은 가입할 수 없습니다.");
		}
		// 3,4등급 가입 불가 로직 구현할 것. Validation.cs::Check 참고할 것.
		// if (is_3_4_gradeBuilding(String.valueOf(cover.get("etcStrct")), String.valueOf(cover.get("etcRoof")))) {
		// 	throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "3,4등급 건물은 가입할 수 없습니다.");
		// }

		String quote_no = QuoteUtil.GetNewQuoteNo("Q");
		try {
			String dong_info = String.valueOf(detail.get("dongNm")) + " " + String.valueOf(detail.get("hoNm"));
			in010tMapper.fireinsurance_insert(
				quote_no,
				building_type,
				getNotEmptyAddress(detail.get("platPlc"), detail.get("newPlatPlc")) + " " + dong_info,
				// String.valueOf(detail.get("newPlatPlc")) + " " + dong_info,
				"S", //(String)cover.get("group_ins"),
				String.valueOf(detail.get("bldNm")),
				dong_info, //dong_info
				String.valueOf(detail.get("mainPurpsCdNm")),
				String.valueOf(detail.get("newPlatPlc")),
				String.valueOf(detail.get("etcPurps")),
				String.valueOf(cover.get("useAprDay")),
				String.valueOf(cover.get("etcRoof")),
				String.valueOf(detail.get("dongNm")),
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
			Map<String, Object> product = in006cMapper.selectByPcode("m002");
			data.put("product", product);
			return data;
		} catch (DataAccessException e) {
			log.error("/house/quotes/sedae:{}, {}", quote_no, e.getMessage());
			if (e.getRootCause() instanceof SQLException) {
				SQLException sqlEx = (SQLException) e.getRootCause();
				int sqlErrorCode = sqlEx.getErrorCode();
				log.error("/house/quotes/sedae:{}, sqlErrorCode:{}", quote_no, sqlErrorCode);
			}
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		} catch (Exception e) {
			log.error("/house/quotes/sedae: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
	}

	private String getNotEmptyAddress(Object platPlc, Object newPlatPlc) {
		String address = InsuStringUtil.IsEmpty(String.valueOf(newPlatPlc)) ? String.valueOf(platPlc) : String.valueOf(newPlatPlc);
		return address;
	}

	// private boolean is_3_4_gradeBuilding(String pillar, String roof) {
	// 	// 기둥리스트에 판넬,샌드위치,목조,철골 있으면 3,4등급
	// 	if (InsuStringUtil.ContainStringInArray(new String[] { "판넬", "샌드위치", "목조" }, pillar)) return true;
	// 	// 기둥리스트에 아래가 있으면 3,4등급 아님
	// 	if (
	// 		!InsuStringUtil.ContainStringInArray(
	// 			new String[] {
	// 				"철근콘",
	// 				"콘크리트",
	// 				"연와",
	// 				"시멘트벽돌",
	// 				"벽조",
	// 				"조적",
	// 				"블록",
	// 				"블럭",
	// 				"벽돌",
	// 				"브록",
	// 				"브럭",
	// 				"세멘",
	// 				"라멘",
	// 			},
	// 			pillar
	// 		)
	// 	) return true;

	// 	// 지붕리스트에  "판넬", "샌드위치", "목조" 가 있고, 아래것이 없으면 3,4등급
	// 	if (
	// 		InsuStringUtil.ContainStringInArray(new String[] { "판넬", "샌드위치", "목조" }, roof) &&
	// 		!InsuStringUtil.ContainStringInArray(
	// 			new String[] {
	// 				"철근콘",
	// 				"연와",
	// 				"시멘트벽돌",
	// 				"세멘",
	// 				"벽조",
	// 				"조적",
	// 				"블록",
	// 				"블럭",
	// 				"브럭",
	// 				"벽돌",
	// 				"스라브",
	// 				"슬라브",
	// 				"슬래브",
	// 				"스래부",
	// 				"스래브",
	// 				"경사지붕",
	// 				"기타지붕",
	// 				"콩크리트",
	// 				"콘크리트",
	// 				"철근콘",
	// 				"평옥개",
	// 				"평판지붕",
	// 			},
	// 			roof
	// 		)
	// 	) return true;
	// 	return false;
	// }

	// 세대가입에서만 호출한다.
	// 따라서 세대별 가입 중 단독주택은 가입할 수 없음을 여기서 미리 체크하여야 뒷단 /sedae 에서 오류가 안 난다.
	@GetMapping(path = "cover")
	public List<Map<String, Object>> cover(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		try {
			Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);

			List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

			Map<String, Object> coverSummary = QuoteUtil.GetCoverSummary(items);

			String building_type = in001tMapper.getBuildingType(
				String.valueOf(coverSummary.get("etcPurps")),
				String.valueOf(coverSummary.get("mainPurpsCdNm")),
				String.valueOf(items.size()),
				String.valueOf(coverSummary.get("max_grnd_flr_cnt")),
				String.valueOf(coverSummary.get("total_area"))
			);

			// 주택화재 보험은 주택에 대해서만 가입 가능하다.
			if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
			}

			// 단독주택은 단체가입으로~~
			if (InsuStringUtil.Equals(building_type, "ILB")) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "단독주택은 단체가입하셔야 합니다.");
			}

			// 다가구주택은 단체가입으로~~
			if (InsuStringUtil.Equals(building_type, "DGG")) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "다가구주택은 단체가입하셔야 합니다.");
			}

			// 16층 이상 건물은 가입할 수 없다.
			if (InsuStringUtil.ToIntOrDefault(coverSummary.get("max_grnd_flr_cnt"), 0) > 15) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "16층 이상 건물은 가입할 수 없습니다.");
			}

			return items;
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
