package com.insrb.app.openservice.api;

import com.insrb.app.openservice.exception.SearchException;
import com.insrb.app.utils.InsuStringUtil;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.json.XML;

import java.util.Map;
import java.util.Objects;

public class InsuroboAddressInfo {
	/**
	 *
	 */
	private static final String APIS_DATA_GO_KR_SERVICE_KEY =
		"uc6vMDUub4APkNN4uC0WT3N19fZaUBPVKIfgiiJmOBXVn+Imupb6vOaFNvI5GYwDAMVdGqvGNbojGRcnH4xV2w==";
	/**
	 *
	 */
	private static final String JUSO_GO_KR_URL = "https://www.juso.go.kr/addrlink/addrLinkApi.do";
	private static final String CONFIRM_KEY = "U01TX0FVVEgyMDIxMDMzMTE1MzA1NTExMDk5Mzc=";

	
	public Map<String, Object> getJusoList(String search) throws SearchException{
		HttpResponse<JsonNode> res = Unirest
			.post(JUSO_GO_KR_URL)
				.field("confmKey", CONFIRM_KEY)
			.field("keyword", search)
			.field("resultType", "json")
			.field("currentPage", "1")
			.field("countPerPage", "1000")
			.field("addressDong", "")
			.field("addressHo", "")
			.asJson();

		JSONObject json = res.getBody().getObject();

		String rep = json.toString().replaceAll("&amp;", "&").replaceAll("&lt;","<").replaceAll("&gt;",">");

		JSONObject json_rep = new JSONObject(rep);

		if(Objects.isNull(json_rep)) throw new SearchException("응답이없습니다");
		String errCode = json_rep.getJSONObject("results").getJSONObject("common").getString("errorCode");
		if(!InsuStringUtil.Equals(errCode, "0")){
			String errorMessage = json_rep.getJSONObject("results").getJSONObject("common").getString("errorMessage");
			throw new SearchException(errorMessage);
		}
		String totalCount = json_rep.getJSONObject("results").getJSONObject("common").getString("totalCount");
		if(InsuStringUtil.Equals(totalCount, "0")){
			throw new SearchException("조회된 데이터가 없습니다.");
		}
		return json_rep.toMap();
	}

	public Map<String, Object> getHouseCoverInfo(String sigunguCd, String bjdongCd, int bun, int ji) {
		String str_bun = String.format("%04d", bun);
		String str_ji = String.format("%04d", ji);

		HttpResponse<String> res = Unirest
			.get("http://apis.data.go.kr/1613000/BldRgstService_v2/getBrTitleInfo")
			.queryString("ServiceKey", APIS_DATA_GO_KR_SERVICE_KEY)
			.queryString("sigunguCd", sigunguCd)
			.queryString("bjdongCd", bjdongCd)
			.queryString("bun", str_bun)
			.queryString("ji", str_ji)
			.queryString("numOfRows", "1000")
			.queryString("pageNo", "1")
			.asString();
		org.json.JSONObject jObject = XML.toJSONObject(res.getBody());
		return jObject.toMap();
	}

	public Map<String, Object> getHouseDetailInfo(String sigungucd, String bjdongcd, int bun, int ji, String dongnm, String honm) {
		String str_bun = String.format("%04d", bun);
		String str_ji = String.format("%04d", ji);

		HttpResponse<String> res = Unirest
			.get("http://apis.data.go.kr/1613000/BldRgstService_v2/getBrExposPubuseAreaInfo")
			.queryString("ServiceKey", APIS_DATA_GO_KR_SERVICE_KEY)
			.queryString("sigunguCd", sigungucd)
			.queryString("bjdongCd", bjdongcd)
			.queryString("bun", str_bun)
			.queryString("ji", str_ji)
			.queryString("dongNm", dongnm)
			.queryString("hoNm", honm)
			.queryString("numOfRows", "1000")
			.queryString("pageNo", "1")
			.asString();
		org.json.JSONObject jObject = XML.toJSONObject(res.getBody());
		return jObject.toMap();
	}

}
