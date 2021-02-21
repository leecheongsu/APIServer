package com.insrb.app.mapper;

import java.util.List;
import java.util.Map;

public interface IN003T_V1Mapper {
	List<Map<String, Object>> selectByUserId(String user_id);

}
