package com.insrb.app.mapper;

import java.util.Map;

public interface OPEN0001 {
	void validateLicence(Map<String, Object> params);
	void resultLogToLicence(String licenceSerialKey, int accessHistoryKey, String resultLog);
}
