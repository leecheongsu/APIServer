package com.insrb.app.mapper;

public interface TermsMapper {
	void insert(
		String user_id,
		String quote_no,
		int termsA_1,
		int termsA_2,
		int termsA_3,
		int termsA_4,
		int termsA_5,
		int termsB_1,
		int termsB_2,
		int termsB_3,
		int termsC_1,
		int termsC_2,
		int termsC_3,
		int termsC_4,
		int termsC_5,
		int termsD_1,
		int termsD_2,
		int termsD_3,
		int termsE_1,
		int termsE_2,
		int termsE_3,
		int termsF_1,
		int termsG_1
	);
	void delete(String quote_no);
}
