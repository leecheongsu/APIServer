package com.insrb.app.util;

import com.insrb.app.exception.SearchException;
import kong.unirest.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrsUtil {

    public static List<Map<String, Object>> GetFromBrs(Map<String, Object> search) throws SearchException {
        Map<String, Object> response = (Map<String, Object>) search.get("response");
        Map<String, Object> header = (Map<String, Object>) response.get("header");

        if(!"00".equals(header.get("resultCode"))) throw new SearchException(
                "검색이 원할하지 않습니다. \n관리자에게 문의하십시요."
        );

        Map<String, Object> body = (Map<String, Object>) response.get("body");
        if (body.get("items") instanceof String) throw new SearchException("해당 건물은 정보가 없습니다.\n관리자에게 문의하십요.");
        Map<String, Object> items = (Map<String, Object>) body.get("items");

        List<Map<String, Object>> item = new ArrayList<Map<String, Object>>();

        // 단건인 경우, XML 파서가 단건인 경우 배열 처리 안하고 넘기는 것 같음.
        if (items.get("item") instanceof HashMap) {
            item.add((Map<String, Object>) items.get("item"));
        } else { //리스트로 올 경우
            item = (List<Map<String, Object>>) items.get("item");
        }
        if (items == null || items.size() < 1) throw new SearchException("해당 물건은 정보가 없습니다.\n관리자에게 문의하십시요.");

        return item;
    }

    public static double GetTotalArea(List<Map<String, Object>> items) {
        double total_area = 0.00;

        for(Map<String, Object> item : items)
        {
            total_area += InsuJsonUtil.IntOrDoubleToDouble(item.get("totArea"));
        }
        return total_area;
    }

    public static Map<String, Object> GetCoverSummary(List<Map<String, Object>> items) {
        int cnt_sedae = 0;
        double total_area = 0.00;
        Object useAprDay = "";
        Map<String, Object> dong_info = new HashMap<String, Object>();

        Map<String, Object> cover = null;
        int max_grnd_flr_cnt = 1;
        int max_sedae = 0;
        for (Map<String, Object> item : items) {
            int sedae = (int) item.get("hhldCnt");
             if (cover == null && sedae > 0) cover = item; // 세대가 한세대라도 있는 건물(동)을 대표로 한다.
            cnt_sedae += sedae;
            total_area += InsuJsonUtil.IntOrDoubleToDouble(item.get("totArea"));
            String dong_name = item.get("dongNm") != "" ? String.valueOf( item.get("dongNm")) : String.valueOf(item.get("bldNm"));
            dong_info.put(dong_name, item.get("totArea"));

            if (item.get("grndFlrCnt") != null) {
                int grnd_flr_cnt = (int) item.get("grndFlrCnt");
                max_grnd_flr_cnt = (grnd_flr_cnt > max_grnd_flr_cnt) ? grnd_flr_cnt : max_grnd_flr_cnt;
            }
            //마지막 데이터의 승인일.
            useAprDay = item.get("useAprDay");


            //많고 많은 표제부 중에서 하나를 선택하는데, 세대수가 가장 많은 표제부를 선택
            if(sedae > max_sedae){
                max_sedae = sedae;
                cover = item;
            }
        }

        if(useAprDay.toString() == ""){
            for(Map<String, Object> item : items) {
                useAprDay = item.get("useAprDay");
                break;
            }
        }

        if (max_sedae == 0) cover = items.get(items.size() -1 );

        cover.put("cnt_sedae", String.valueOf(cnt_sedae));
        cover.put("total_area", String.valueOf(total_area));
        cover.put("dong_info", new JSONArray().put(dong_info).toString());
        cover.put("max_grnd_flr_cnt", String.valueOf(max_grnd_flr_cnt));
        cover.put("useAprDay", useAprDay);
        return cover;
    }

}
