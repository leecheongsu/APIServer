package com.insrb.app.mapper;


public interface IN009TMapper {
	void insert(
		String receipt_id,
		String quote_no,
		String pg,
		String pg_name,
		String method,
		String method_name,
		String name,
		String order_id,
		String receipt_url,
		String payment_data_card_name,
		String payment_data_card_no,
		String payment_data_card_quota,
		String payment_data_card_auth_no,
		String payment_data_vbank_bankname,
		String payment_data_vbank_accountholder,
		String payment_data_vbank_account,
		String payment_data_vbank_expiredate,
		String payment_data_vbank_username,
		String payment_data_vbank_cash_result,
		String price,
		String status,
		String payment_json
	);
}
