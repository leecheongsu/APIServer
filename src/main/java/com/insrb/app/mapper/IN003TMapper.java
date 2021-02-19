package com.insrb.app.mapper;

import java.util.Date;
import java.util.HashMap;

public interface IN003TMapper {
	void insert(
		String quote_no,
		String prod_code,
		long amt_ins,
		int opayment,
		String polholder,
		String insurant_a,
		String insurant_b,
		int premium,
		Date insdate,
		Date ins_from,
		Date ins_to,
		String ptype,
		String insloc,
		String mobile,
		String email,
		String poption,
		String v_bank_name,
		String v_bank_no,
		String v_bank_due_date,
		String pbohumja_mobile,
		String jumin,
		String owner,
		String pbohumja_birth,
		String advisor_no,
		String already_group_ins
		);
	void delete(String quote_no);

	HashMap<String, Object> selectById(String quote_no);

}
