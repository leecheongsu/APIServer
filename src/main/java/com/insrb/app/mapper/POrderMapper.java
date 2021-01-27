package com.insrb.app.mapper;

import java.sql.Date;

public interface POrderMapper {
	void insert(
		String quote_no,
		String prod_code,
		String opayment,
		String polholder,
		String insurant_a,
		String insurant_b,
		String premium,
		String insdate,
		Date ins_from,
		Date ins_to,
		int ptype,
		String insloc,
		String mobile,
		String email,
		int poption,
		String pbohumja_mobile,
		String pbohumja_teltype,
		Date odate,
		String jumin,
		String user_id,
		int o_by,
		String owner,
		String pbohumja_birth,
		String advisor_no
	);
	void delete(String quote_no);
}
