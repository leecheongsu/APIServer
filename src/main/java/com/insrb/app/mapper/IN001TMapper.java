package com.insrb.app.mapper;

import java.util.HashMap;

public interface IN001TMapper {
	HashMap<String, Object> selectById(String quote_no);
	String getBuildingType(String etc_purps, String main_purps_cd_nm, String p_cnt, String p_max_grnd_cnt, String p_total_area);
}
