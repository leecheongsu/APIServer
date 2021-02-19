package com.insrb.app.mapper;

public interface IN009TMapper {
	void insertCard(
		String quote_no,
		String tid,
		String resultcode,
		String resultmsg,
		String paydate,
		String paytime,
		String payauthcode,
		String cardcode,
		String checkflg,
		String payauthquota,
		String prtccode,
		int price
	);

	void insertVacct(
		String quote_no,
		String tid,
		String resultcode,
		String resultmsg,
		String vact_auth_dt,
		String vact_fn_cd1,
		int price
	);

	void delete(String tid,String resultcode);
}
