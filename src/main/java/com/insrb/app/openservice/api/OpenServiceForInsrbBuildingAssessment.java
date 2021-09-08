package com.insrb.app.openservice.api;

import com.insrb.app.openservice.api.InsuroboOpenApiRules.InsuroboOpenApiRule;
import com.insrb.app.openservice.database.InsuroboDataBaseAccess;
import com.insrb.app.openservice.exception.SearchException;
import com.insrb.app.utils.InsuJsonUtil;
import com.insrb.app.utils.QuoteUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class OpenServiceForInsrbBuildingAssessment {

	public Map<String, Object> quoteAssessmentForGroup(InsuroboOpenApi access, String sigungucd, String bjdongcd, int bun, int ji) {
		if(!access.isValidated()) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "인슈로보 REST API 라이센스가 맞지 않습니다.");
		}
		logDebug("quotes/danche:{},{},{},{}", sigungucd, bjdongcd, bun, ji);
		try {
			Map<String, Object> search = new InsuroboAddressInfo().getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
			
			List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

			// if (items == null || items.size() < 1) throw new
			// ResponseStatusException(HttpStatus.NO_CONTENT);
			Map<String, Object> cover = QuoteUtil.GetCoverSummary(items);
			
			

			String building_type = access.getDataAccess().getBuildingType().getBuildingType(String.valueOf(cover.get("etcPurps")),
					String.valueOf(cover.get("mainPurpsCdNm")), String.valueOf(items.size()),
					String.valueOf(cover.get("max_grnd_flr_cnt")), String.valueOf(cover.get("total_area")));

			
			InsuroboOpenApiRule thisRule= InsuroboOpenApiRules.getCurrentRule("OpenServiceForInsrbBuildingAssessment.quoteAssessmentForGroup");
			// 주택화재 보험은 주택에 대해서만 가입 가능하다.
			
			if(thisRule.useExceptionCaseRule("building_type:null")) {
				if(building_type==null) {
					building_type="APT";
				}
			}
			
//			if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
//				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
//			}
			// 16층 이상 건물은 가입할 수 없다.
//			if (InsuStringUtil.ToIntOrDefault(cover.get("max_grnd_flr_cnt"), 0) > 15) {
//				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "16층 이상 건물은 가입할 수 없습니다.");
//			}

//			if(thisRule.useExceptionCaseRule("mainPurpsCdNm:근린")) {
//				if (String.valueOf(cover.get("mainPurpsCdNm")).contains("근린")) {
//					throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "해당 건물은 일반화재상품으로 가입할 수 있습니다");
//				}
//			}

			String quote_no = QuoteUtil.GetNewQuoteNo("Q");
			access.getDataAccess().getInsuranceRegistration().fireinsurance_insert(quote_no, building_type,
					QuoteUtil.GetNotEmptyAddress(cover.get("platPlc"), cover.get("newPlatPlc")), "T", // (String)cover.get("group_ins"),
					String.valueOf(cover.get("bldNm")), String.valueOf(cover.get("dong_info")),
					String.valueOf(cover.get("mainPurpsCdNm")), String.valueOf(cover.get("newPlatPlc")),
					String.valueOf(cover.get("etcPurps")), String.valueOf(cover.get("useAprDay")),
					String.valueOf(cover.get("etcRoof")), String.valueOf(cover.get("dongNm")),
					String.valueOf(cover.get("total_area")), String.valueOf(cover.get("cnt_sedae")),
					String.valueOf(cover.get("grndFlrCnt")), String.valueOf(cover.get("ugrndFlrCnt")),
					String.valueOf(cover.get("etcStrct")), cover.toString(), "" // 단체가입은 전유부가 없다.
			);

			// 단독주택(단독주택,다가구주택) 중 5억 이상은 가입 불가
			Map<String, Object> data = access.getDataAccess().getBuildingType().selectById(quote_no);
			
//			if (InsuStringUtil.Equals(building_type, "ILB") || InsuStringUtil.Equals(building_type, "DGG")) {
//				if (InsuStringUtil.ToIntOrDefault(data.get("amt_ins"), 0) > 500000000) {
//					throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "보험가입금액 5억 이상 단독/다가구주택은 가입할 수 없습니다.");
//				}
//			}

			List<Map<String, Object>> detail = access.getDataAccess().getPremiums().selectById(quote_no);
			data.put("premiums", detail);
			Map<String, Object> product = access.getDataAccess().getProducts().selectByPcode("m002");
			data.put("product", product);
			
			access.commit("quoteAssessmentForGroup","quote_no="+quote_no,data,sigungucd,bjdongcd,bun,ji);
			//remove rule 추가할 것. 2021.07.13 이청수

			data.remove("otwl_strc");
			data.remove("tariff_id");
			data.remove("total_area");
			data.remove("lsgc_cd");
			data.remove("cnt_sedae");
			data.remove("use_apr_date");
			data.remove("const_price");
			data.remove("group_ins");
			data.remove("dr");
			data.remove("cover_info");
			data.remove("calc_text");
			data.remove("product");
			data.remove("amt_premium");
			data.remove("gt_num");
			data.remove("already_group_ins");
			data.remove("premiums");
			data.remove("flr_name");
			data.remove("main_purps");
			data.remove("user_id");
			data.remove("bld_name");
			data.remove("roof_name");
			data.remove("quote_date");
			data.remove("quote_no");
			data.remove("building_type");

			return data;
		} catch (DataAccessException e) {
			if (e.getRootCause() instanceof SQLException) {
				SQLException sqlEx = (SQLException) e.getRootCause();
				int sqlErrorCode = sqlEx.getErrorCode();
				if (sqlErrorCode == -20101) {
					logError("quotes/danche(신축단가조회오류):{},{},{},{}", e, sigungucd, bjdongcd, bun, ji);
					throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
							"등록된 건물에 대한 단가를 찾지못했습니다.\n관리자에게 연락해주세요.");
				}
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, sqlEx.getMessage());
			}
			logError("/house/quotes/danche: DataAccessException", e);
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		} catch (SearchException e) {
			logError("/house/quotes/danche: {}", e);
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "시스템 점검중입니다");
		}
	}



	public Map<String, Object> quoteAssessmentForSingle(InsuroboOpenApi access, Map<String, Object> cover, Map<String, Object> detail) {
		if(!access.isValidated()) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "인슈로보 REST API 라이센스가 맞지 않습니다.");
		}
		if (cover == null)
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "표제부가 있어야합니다.");
		if (detail == null)
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "전유부가 있어야합니다.");
		
		

		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		Double tot_area = InsuJsonUtil.IntOrDoubleToDouble(cover.get("totArea"));
		String building_type = access.getDataAccess().getBuildingType().getBuildingType(String.valueOf(cover.get("etcPurps")),
				String.valueOf(cover.get("mainPurpsCdNm")), "1", String.valueOf(cover.get("grndFlrCnt")),
				String.valueOf(tot_area));

		InsuroboOpenApiRule thisRule=InsuroboOpenApiRules.getCurrentRule("OpenServiceForInsrbBuildingAssessment.quoteAssessmentForSingle");
		// 주택화재 보험은 주택에 대해서만 가입 가능하다.
		
		if(thisRule.useExceptionCaseRule("building_type:null")) {
			if(building_type==null) {
				building_type="APT";
			}
		}
//		if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
//			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
//		}
		// 단독주택은 단체가입으로~~
//		if (InsuStringUtil.Equals(building_type, "ILB")) {
//			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "단독주택은 단체가입하셔야 합니다.");
//		}
//		int cnt_sedae = (int) cover.get("hhldCnt");
//		if (cnt_sedae < 1) {
//			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "표제부 세대수가 1 이상이어야 합니다.");
//		}

		// 개별 세대의 면적으로 치환
		tot_area = InsuJsonUtil.IntOrDoubleToDouble(detail.get("area"));

		// if (InsuStringUtil.IsEmpty(building_type)) {
		// throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입
		// 가능합니다.");
		// }
//		if (InsuStringUtil.ToIntOrDefault(cover.get("grndFlrCnt"), 0) > 15) {
//			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "16층 이상 건물은 가입할 수 없습니다.");
//		}
		// 3,4등급 가입 불가 로직 구현할 것. Validation.cs::Check 참고할 것.
		// if (is_3_4_gradeBuilding(String.valueOf(cover.get("etcStrct")),
		// String.valueOf(cover.get("etcRoof")))) {
		// throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "3,4등급 건물은 가입할 수
		// 없습니다.");
		// }

		String quote_no = QuoteUtil.GetNewQuoteNo("Q");
		try {
			String dong_info = String.valueOf(detail.get("dongNm")) + " " + String.valueOf(detail.get("hoNm"));
			access.getDataAccess().getInsuranceRegistration().fireinsurance_insert(quote_no, building_type,
					QuoteUtil.GetNotEmptyAddress(detail.get("platPlc"), detail.get("newPlatPlc")) + " " + dong_info,
					// String.valueOf(detail.get("newPlatPlc")) + " " + dong_info,
					"S", // (String)cover.get("group_ins"),
					String.valueOf(detail.get("bldNm")), dong_info, // dong_info
					String.valueOf(detail.get("mainPurpsCdNm")), String.valueOf(detail.get("newPlatPlc")),
					String.valueOf(detail.get("etcPurps")), String.valueOf(cover.get("useAprDay")),
					String.valueOf(cover.get("etcRoof")), String.valueOf(detail.get("dongNm")),
					String.valueOf(tot_area), "1", // String.valueOf(cover.get("cnt_sedae")),
					String.valueOf(cover.get("grndFlrCnt")), String.valueOf(cover.get("ugrndFlrCnt")),
					String.valueOf(cover.get("etcStrct")), cover.toString(), detail.toString());

			Map<String, Object> data = access.getDataAccess().getBuildingType().selectById(quote_no);
			List<Map<String, Object>> in002t = access.getDataAccess().getPremiums().selectById(quote_no);
			data.put("premiums", in002t);
			Map<String, Object> product = access.getDataAccess().getProducts().selectByPcode("m002");
			data.put("product", product);
			
			access.commit("quoteAssessmentForSingle","quote_no="+quote_no,data,cover,detail);

			data.remove("otwl_strc");
			data.remove("tariff_id");
			data.remove("total_area");
			data.remove("lsgc_cd");
			data.remove("pole_strc");
			data.remove("cnt_sedae");
			data.remove("use_apr_date");
			data.remove("const_price");
			data.remove("group_ins");
			data.remove("dr");
			data.remove("cover_info");
			data.remove("detail_info");
			data.remove("calc_text");
			data.remove("product");
			data.remove("amt_premium");
			data.remove("gt_num");
			data.remove("already_group_ins");
			data.remove("premiums");
			data.remove("flr_name");
			data.remove("main_purps");
			data.remove("dong_info");
			data.remove("user_id");
			data.remove("roof_name");
			data.remove("bld_name");
			data.remove("quote_date");
			data.remove("quote_no");
			data.remove("building_type");


			return data;
		} catch (DataAccessException e) {
			logError("/house/quotes/sedae:{}, {}", e, quote_no);
			if (e.getRootCause() instanceof SQLException) {
				SQLException sqlEx = (SQLException) e.getRootCause();
				int sqlErrorCode = sqlEx.getErrorCode();
				if (sqlErrorCode == -20101) {
					logError("/house/quotes/sedae(신축단가조회오류):{}", e, quote_no);
					throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
							"등록된 건물에 대한 단가를 찾지못했습니다.\n관리자에게 연락해주세요.");
				}
				logError("/house/quotes/sedae:{}, sqlErrorCode:{}", e, quote_no, sqlErrorCode);
				throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, sqlEx.getMessage());
			}
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		} catch (Exception e) {
			logError("/house/quotes/sedae: {}", e);
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "시스템 점검중입니다");
		}
	}



	public List<Map<String, Object>> quoteHouseInfoCovers(InsuroboDataBaseAccess dba, String sigungucd, String bjdongcd, int bun, int ji) {
		
		try {
			Map<String, Object> search = new InsuroboAddressInfo().getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);


			List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

			Map<String, Object> coverSummary = QuoteUtil.GetCoverSummary(items);

			String building_type = dba.getBuildingType().getBuildingType(String.valueOf(coverSummary.get("etcPurps")),
					String.valueOf(coverSummary.get("mainPurpsCdNm")), String.valueOf(items.size()),
					String.valueOf(coverSummary.get("max_grnd_flr_cnt")),
					String.valueOf(coverSummary.get("total_area")));

			// 주택화재 보험은 주택에 대해서만 가입 가능하다.
//			if (InsuStringUtil.IsEmpty(building_type) || InsuStringUtil.Equals(building_type, "ETC")) {
//				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "주택만 가입 가능합니다.");
//			}
//
			// 단독주택은 단체가입으로~~
//			if (InsuStringUtil.Equals(building_type, "ILB")) {
//				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "단독주택은 단체가입하셔야 합니다.");
//			}

			// 다가구주택은 단체가입으로~~
//			if (InsuStringUtil.Equals(building_type, "DGG")) {
//				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "다가구주택은 단체가입하셔야 합니다.");
//			}

			// 16층 이상 건물은 가입할 수 없다.
			// if (InsuStringUtil.ToIntOrDefault(coverSummary.get("max_grnd_flr_cnt"), 0) >
			// 15) {
			// throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "16층 이상 건물은 가입할
			// 수 없습니다.");
			// }


			return items;
		} catch (SearchException e) {
			logError("cover : {}", e);
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "시스템 점검중입니다");
		}
	}

	public List<Map<String, Object>> quoteHouseInfoDetails(String sigungucd, String bjdongcd, int bun, int ji, String dongnm, String honm) {
		Map<String, Object> search = new InsuroboAddressInfo().getHouseDetailInfo(sigungucd, bjdongcd, bun, ji, dongnm, honm);
		List<Map<String, Object>> items = QuoteUtil.getDetailItemFromHouseInfo(search);

		return items;
	}
	
	
	
	protected void logDebug(String tag, Object... message) {
		
	}
	protected void logError(String tag, Exception ex, Object... message) {
		
	}

}
