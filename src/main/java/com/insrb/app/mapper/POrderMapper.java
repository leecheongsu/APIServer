package com.insrb.app.mapper;

import java.util.Date;
import java.util.HashMap;

public interface POrderMapper {
	void insert(
		String quote_no,
		String prod_code,
		int opayment,
		String polholder,
		String insurant_a,
		String insurant_b,
		int premium,
		String insdate,
		Date ins_from,
		Date ins_to,
		int ptype,
		String insloc,
		String mobile,
		String email,
		String pbohumja_mobile,
		String jumin,
		String user_id,
		int o_by,
		String owner,
		String pbohumja_birth,
		int advisor_no
	);
	void delete(String quote_no);

	HashMap<String, Object> selectById(String quote_no);

}
