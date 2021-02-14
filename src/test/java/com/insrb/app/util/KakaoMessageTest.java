package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class KakaoMessageTest {

	@Test
	public void test_A001() {
		String phone = "01047017956";
		// String msg =
		// 	"안녕하세요 #{홍길동}님.\r\n#{보험가입상품명}\r\n계약 체결이 완료되었습니다.\r\n\r\n★보험가입 요약정보★\r\n-.계약자 : #{홍길동}\r\n-.피보험자 : #{변사또}\r\n-.피보험목적물 :#{부산시 남구 전포대로 133, ㅇㅇㅇ}\r\n-.보험가입금액 : #{000,000,000}원\r\n-.납입보험료 : #{00,000}원\r\n-.증권번호 : #{0000000000000}\r\n-.보험계약체결일 : #{2020.07.10}\r\n-.보험시작일 : #{2020.07.11}\r\n-.보험기간 : #{2020.07.11 24:00 ~ 2021.07.10 24:00}\r\n\r\n※약관 및 증권은 #{info@insurobo.co.kr}로 전송되었습니다.\r\n기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070-4126-3333)로\r\n문의 바랍니다.";
		String u_name = "김종호";
		String product = "보험가입상품명";
		String pu_name = "변사또";
		String pu_insloc = "부산시 남구 전포대로 133";
		String allamount = "12345678";
		String price = "1,567";
		String success_num = "보험가입상품명";
		String success = "2020.07.10";
		String start_date = "2020.07.10";
		String period = "2020.07.11 24:00 ~ 2021.07.10 24:00";
		assertEquals(
			true,
			KakaoMessageUtil.A001(phone, u_name, product, pu_name, pu_insloc, allamount, price, success_num, success, start_date, period)
		);
	}

	@Test
	public void test_A002() {
		String phone = "01047017956";
		String u_name = "김종호";
		String p_name = "보험가입상품명";
		String b_name = "KB국민은행";
		String account_num = "1231242354234534";
		String amt = "123,123";
		String last_date = "2020년 7월 10일 23시 59분 00초";
        // String msg =
		// 	"안녕하세요 #{홍길동}님.\r\n#{보험가입상품명} 계약건에 대한 보험료를\r\n이체하실 계좌번호 정보를 알려드립니다.\r\n\r\n-.거래은행 : #{KB국민은행}\r\n-.계좌주명 : 주식회사 인슈로보\r\n-.계좌번호(가상계좌) : #{02649071247314}\r\n-.입금하실 보험료 : #{00,000}원\r\n-.입금마감일자 : #{2020년 7월 10일 23시 59분 00초}\r\n\r\n※입금마감일자가 지나면 계좌가 자동 폐쇄되어 입금이 되지 않고,\r\n 계약이 완료되지 않습니다. 유의하시어 기간 내에 처리 바랍니다.";

		assertEquals(true, KakaoMessageUtil.A002(phone, u_name, p_name, b_name, account_num, amt, last_date));
	}

	@Test
	public void test_AI001() {
		String phone = "01047017956";
		// String msg =
		// 	"안녕하세요 #{홍길동}님.\r\n#{보험가입상품명}\r\n계약 체결이 완료되었습니다.\r\n\r\n★보험가입 요약정보★\r\n-.계약자 : #{홍길동}\r\n-.피보험자 : #{변사또}\r\n-.피보험목적물 :#{부산시 남구 전포대로 133, ㅇㅇㅇ}\r\n-.보험가입금액 : #{000,000,000}원\r\n-.납입보험료 : #{00,000}원\r\n-.증권번호 : #{0000000000000}\r\n-.보험계약체결일 : #{2020.07.10}\r\n-.보험시작일 : #{2020.07.11}\r\n-.보험기간 : #{2020.07.11 24:00 ~ 2021.07.10 24:00}\r\n\r\n※약관 및 증권은 info@insurobo.co.kr로 전송되었습니다.\r\n기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070-4126-3333)로\r\n문의 바랍니다.";
            String			u_name = "김종호";
            String			p_name = "보험가입상품명";
            String			pu_name = "변사또";
            String			pu_insloc = "부산시 남구 전포대로 133";
            String			allamount = "123,123";
            String			price = "5,432";
            String			success_num = "Q000000000000";
            String			success = "2020.07.10";
            String			start_date = "2020.07.21";
            String			period = "2020.07.11 24:00 ~ 2021.07.10 24:00";
		assertEquals(true, KakaoMessageUtil.AI001(phone, u_name, p_name, pu_name, pu_insloc, allamount, price, success_num, success, start_date, period));
	}
}
