package com.insrb.app.mapper;

public interface IN009TMapper {
	void insert(
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
}
