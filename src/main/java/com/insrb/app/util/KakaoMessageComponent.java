package com.insrb.app.util;

import com.insrb.app.mapper.IN901TMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * http://api.apistore.co.kr/kko/1/template/list/insurobo
 * A001, AI001, A002 
{
    "result_code": "200",
    "result_message": "success",
    "templateList": [
        {
            "template_code": "A001",
            "template_name": "계약완료",
            "template_msg": "안녕하세요 #{홍길동}님.\n#{보험가입상품명}\n계약 체결이 완료되었습니다.\n\n★보험가입 요약정보★\n-.계약자 : #{홍길동}\n-.피보험자 : #{변사또}\n-.피보험목적물 :#{부산시 남구 전포대로 133, ㅇㅇㅇ}\n-.보험가입금액 : #{000,000,000}원\n-.납입보험료 : #{00,000}원\n-.증권번호 : #{0000000000000}\n-.보험계약체결일 : #{2020.07.10}\n-.보험시작일 : #{2020.07.11}\n-.보험기간 : #{2020.07.11 24:00 ~ 2021.07.10 24:00}\n\n※약관 및 증권은 #{info@insurobo.co.kr}로 전송되었습니다.\n기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070-4126-3333)로\n문의 바랍니다.",
            "status": "승인",
            "btnList": []
        },
        {
            "template_code": "A002",
            "template_name": "계좌이체",
            "template_msg": "안녕하세요 #{홍길동}님.\n#{보험가입상품명} 계약건에 대한 보험료를\n이체하실 계좌번호 정보를 알려드립니다.\n\n-.거래은행 : #{KB국민은행}\n-.계좌주명 : 주식회사 인슈로보\n-.계좌번호(가상계좌) : #{02649071247314}\n-.입금하실 보험료 : #{00,000}원\n-.입금마감일자 : #{2020년 7월 10일 23시 59분 00초}\n\n※입금마감일자가 지나면 계좌가 자동 폐쇄되어 입금이 되지 않고,\n 계약이 완료되지 않습니다. 유의하시어 기간 내에 처리 바랍니다.",
            "status": "승인",
            "btnList": []
        },
        {
            "template_code": "AI001",
            "template_name": "완료",
            "template_msg": "안녕하세요 #{홍길동}님.\n#{보험가입상품명}\n계약 체결이 완료되었습니다.\n\n★보험가입 요약정보★\n-.계약자 : #{홍길동}\n-.피보험자 : #{변사또}\n-.피보험목적물 :#{부산시 남구 전포대로 133, ㅇㅇㅇ}\n-.보험가입금액 : #{000,000,000}원\n-.납입보험료 : #{00,000}원\n-.증권번호 : #{0000000000000}\n-.보험계약체결일 : #{2020.07.10}\n-.보험시작일 : #{2020.07.11}\n-.보험기간 : #{2020.07.11 24:00 ~ 2021.07.10 24:00}\n\n※약관 및 증권은 info@insurobo.co.kr로 전송되었습니다.\n기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070-4126-3333)로\n문의 바랍니다.",
            "status": "승인",
            "btnList": [
                {
                    "template_btn_type": "메시지전달",
                    "template_btn_name": "완료",
                    "template_btn_url": null,
                    "template_btn_url2": null
                }
            ]
        },

        { 
            "template_code": "AI002",
            "template_name": "가상계좌",
            "template_msg": "안녕하세요 #{홍길동}님.\n#{보험가입상품명} 계약건에 대한 보험료를\n이체하실 계좌번호 정보를 알려드립니다.\n\n-.거래은행 : #{KB국민은행}\n-.계좌주명 : 주식회사 인슈로보\n-.계좌번호(가상계좌) : #{02649071247314}\n-.입금하실 보험료 : #{00,000}원\n-.입금마감일자 : #{2020년 7월 10일 23시 59분 00초}\n\n※입금마감일자가 지나면 계좌가 자동 폐쇄되어 입금이 되지 않고,\n 계약이 완료되지 않습니다. 유의하시어 기간 내에 처리 바랍니다.",
            "status": "승인",
            "btnList": [
                {
                    "template_btn_type": "메시지전달",
                    "template_btn_name": "가상계좌",
                    "template_btn_url": null,
                    "template_btn_url2": null
                }
            ]
        },
        {
            "template_code": "only",
            "template_name": "only",
            "template_msg": "안녕하세요 인슈로보입니다. \n#{홍길동} 고객님.\n현대해상 풍수해Ⅵ(소상공인) 보험 가입 설계 주소 알려드립니다. \n아래 주소를 선택하시면 바로 연결됩니다.\nURL : #{URL}",
            "status": "승인",
            "btnList": []
        },
        {
            "template_code": "login",
            "template_name": "login",
            "template_msg": "안녕하세요  \n#{홍길동} 고객님.\n인슈로보 앱을 다운로드 받으신 후 로그인 하시면 가입내역을 확인 하실 수 있습니다.\n고객님의 로그인용 이메일 및 비밀번호는 다음과 같습니다. \n이메일 : #{email}\n비밀번호 : #{pwd}\n비밀번호는 꼭 변경하시어 사용하시기 바랍니다.",
            "status": "승인",
            "btnList": []
        },
        {
            "template_code": "newtalk",
            "template_name": "newtalk",
            "template_msg": "안녕하세요 인슈로보입니다. \n#{홍길동} 고객님.\n2021년 확대된 할인율을 적용한 현대해상 풍수해Ⅵ(소상공인) 보험 가입 설계 \n주소 알려드립니다. \n아래 주소를 선택하시면 바로 연결됩니다.\nURL : #{URL}",
            "status": "반려",
            "btnList": []
        }
    ]
} 
*/

@Slf4j
@Component
public class KakaoMessageComponent {

	private static final String URL = "http://api.apistore.co.kr/kko/1/msg/insurobo";
	private static final String CALLBACK = "07041263333";
	private static final String FORM_URLENCODED_UTF_8 = "application/x-www-form-urlencoded; charset=UTF-8";
	private static final String KEY = "MTMyNzQtMTU5NDI2OTUyNzg2Mi04ZGZmMWZlYS1mY2QwLTQ4ODItYmYxZi1lYWZjZDA5ODgyNzY=";

	@Autowired
	IN901TMapper in901tMapper;

	// 계약완료
	public boolean A001(
		String quote_no,
		String phone,
		String u_name,
		String product,
		String pu_name,
		String pu_insloc,
		String allamount,
		String price,
		String success_num,
		String success,
		String start_date,
		String period
	) {
		String msg =
			"안녕하세요" +
			u_name +
			"님.\n" +
			product +
			" 계약 체결이 완료되었습니다.\n\n★보험가입 요약정보★\n" +
			"-.계약자 : " +
			u_name +
			"\n-.피보험자 : " +
			pu_name +
			"\n-.피보험목적물 : " +
			pu_insloc +
			"\n-.보험가입금액 : " +
			allamount +
			"원\n-.납입보험료 : " +
			price +
			"원\n-.증권번호 : " +
			success_num +
			"\n-.보험계약체결일 : " +
			success +
			"\n-.보험시작일 : " +
			start_date +
			"\n-.보험기간 : " +
			period +
			"\n\n※약관 및 증권은 info@insurobo.co.kr로 전송되었습니다.기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070 - 4126 - 3333)로 문의 바랍니다.";

		HttpResponse<JsonNode> res = Unirest
			.post(URL)
			.header("x-waple-authorization", KEY)
			.header("Content-Type", FORM_URLENCODED_UTF_8)
			.field("PHONE", phone)
			.field("CALLBACK", CALLBACK)
			.field("MSG", msg)
			.field("TEMPLATE_CODE", "A001")
			.field("FAILED_TYPE", "N")
			.field("BTN_TYPES", "")
			.field("BTN_TXTS", "계약완료")
			.field("BTN_URLS1", "")
			.asJson();
		if (res.getStatus() == 200) {
			// {"result_message":"OK","result_code":"200","cmid":"2021021411351886720601"}
			JSONObject json = res.getBody().getObject();
			log.debug("KakaoMessage.A001 성공:{}", json.toString());
			in901tMapper.insert(
				quote_no,
				"A001",
				"계약완료",
				phone,
				json.getString("result_code"),
				json.getString("result_message"),
				json.getString("cmid"),
				msg
			);

			return true;
		} else {
			log.error("KakaoMessage.A001 오류:{}", res.getStatusText());
			return false;
		}
	}

	// 계좌이체 안내
	public boolean A002(
		String quote_no,
		String phone,
		String u_name,
		String p_name,
		String b_name,
		String account_num,
		String amt,
		String last_date
	) {
		String msg =
			"안녕하세요" +
			u_name +
			"님.\n" +
			p_name +
			"계약건에 대한 보험료를 이체하실 계좌번호 정보를 알려드립니다.\n\n-.거래은행 : " +
			b_name +
			"\n-.계좌주명 : 주식회사 인슈로보" +
			"\n-.계좌번호(가상계좌) : \n " +
			account_num +
			"\n-.입금하실 보험료 : " +
			amt +
			"원 \n-.입금마감일자 : \n " +
			last_date +
			"\n\n※입금마감일자가 지나면 계좌가 자동 폐쇄되어 입금이 되지 않고,계약이 완료되지 않습니다.\n유의하시어 기간 내에 처리 바랍니다.";

		HttpResponse<JsonNode> res = Unirest
			.post(URL)
			.header("x-waple-authorization", KEY)
			.header("Content-Type", FORM_URLENCODED_UTF_8)
			.field("PHONE", phone)
			.field("CALLBACK", CALLBACK)
			.field("MSG", msg)
			.field("TEMPLATE_CODE", "A002")
			.field("FAILED_TYPE", "N")
			.field("BTN_TYPES", "")
			.field("BTN_TXTS", "계좌이체")
			.field("BTN_URLS1", "")
			.asJson();
		if (res.getStatus() == 200) {
			// {"result_message":"OK","result_code":"200","cmid":"2021021411351886720601"}
			JSONObject json = res.getBody().getObject();
			log.debug("KakaoMessage.A002 성공:{}", json.toString());
			in901tMapper.insert(
				quote_no,
				"A002",
				"가상계좌이체안내",
				phone,
				json.getString("result_code"),
				json.getString("result_message"),
				json.getString("cmid"),
				msg
			);

			return true;
		} else {
			log.error("KakaoMessage.A002 오류:{}", res.getStatusText());
			return false;
		}
	}

	// 계약만료
	public boolean A003(String quote_no, String phone, String u_name, String product, String exp_day, String success_num, String period) {
		String msg =
			"안녕하세요  \r\n" +
			u_name +
			" 고객님.\r\n인슈로보 " +
			product +
			" 의 보험기간 만료일이 " +
			exp_day +
			" 전입니다.\r\n\r\n-.증권번호 : " +
			success_num +
			"\r\n-.보험기간 : " +
			period +
			"\r\n\r\n※기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070-4126-3333)로\r\n문의 바랍니다.";

		HttpResponse<JsonNode> res = Unirest
			.post(URL)
			.header("x-waple-authorization", KEY)
			.header("Content-Type", FORM_URLENCODED_UTF_8)
			.field("PHONE", phone)
			.field("CALLBACK", CALLBACK)
			.field("MSG", msg)
			.field("TEMPLATE_CODE", "A003")
			.field("FAILED_TYPE", "N")
			.field("BTN_TYPES", "")
			.field("BTN_TXTS", "계약만료")
			.field("BTN_URLS1", "")
			.asJson();
		if (res.getStatus() == 200) {
			// {"result_message":"OK","result_code":"200","cmid":"2021021411351886720601"}
			JSONObject json = res.getBody().getObject();
			log.debug("KakaoMessage.A003 성공:{}", json.toString());
			in901tMapper.insert(
				quote_no,
				"A003",
				"계약만료",
				phone,
				json.getString("result_code"),
				json.getString("result_message"),
				json.getString("cmid"),
				msg
			);

			return true;
		} else {
			log.error("KakaoMessage.A003 오류:{}", res.getStatusText());
			return false;
		}
	}

	//  가상계좌 결제완료
	public boolean AI001(
		String quote_no,
		String phone,
		String u_name,
		String p_name,
		String pu_name,
		String pu_insloc,
		String allamount,
		String price,
		String success_num,
		String success,
		String start_date,
		String period
	) {
		String msg =
			"안녕하세요 " +
			u_name +
			"님.\n" +
			p_name +
			"\n계약 체결이 완료되었습니다.\n\n★보험가입 요약정보★" +
			"\n-.계약자 : " +
			u_name +
			"\n-.피보험자 : " +
			pu_name +
			"\n-.피보험목적물 :" +
			pu_insloc +
			"\n-.보험가입금액 : " +
			allamount +
			"원\n-.납입보험료 : " +
			price +
			"원\n-.증권번호 : " +
			success_num +
			"\n-.보험계약체결일 : " +
			success +
			"\n-.보험시작일 : " +
			start_date +
			"\n-.보험기간 : " +
			period +
			"\n\n※약관 및 증권은 info@insurobo.co.kr로 전송되었습니다.\n기타 문의 사항은 인슈로보 문의하기 또는 고객센터(070-4126-3333)로\n문의 바랍니다.";
		log.debug(msg);
		HttpResponse<JsonNode> res = Unirest
			.post(URL)
			.header("x-waple-authorization", KEY)
			.header("Content-Type", FORM_URLENCODED_UTF_8)
			.field("PHONE", phone)
			.field("CALLBACK", CALLBACK)
			.field("MSG", msg)
			.field("TEMPLATE_CODE", "AI001")
			.field("FAILED_TYPE", "N")
			.field("BTN_TYPES", "메시지전달")
			.field("BTN_TXTS", "완료")
			.field("BTN_URLS1", "")
			.asJson();
		if (res.getStatus() == 200) {
			// {"result_message":"OK","result_code":"200","cmid":"2021021411351886720601"}
			JSONObject json = res.getBody().getObject();
			log.debug("KakaoMessage.AI001 성공:{}", json.toString());
			in901tMapper.insert(
				quote_no,
				"AI001",
				"가상계좌입금",
				phone,
				json.getString("result_code"),
				json.getString("result_message"),
				json.getString("cmid"),
				msg
			);
			return true;
		} else {
			log.error("KakaoMessage.AI001 네트웍오류:{}", res.getStatusText());
			return false;
		}
	}
}
