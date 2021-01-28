package com.insrb.app.mapper;

import java.util.Date;

public interface PhoneCertificateDataMapper {
	void insert(
		String rslt_name,
		String rslt_birthday,
		String rslt_sex_cd,
		String rslt_ntv_frnr_cd,
		String di,
		String ci,
		String ci_update,
		String tel_com_cd,
		String tel_no,
		String return_msg
	);
	void delete(String quote_no);
}
