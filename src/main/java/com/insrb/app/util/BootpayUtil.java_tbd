package com.insrb.app.util;

import com.insrb.app.exception.BootpayException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootpayUtil {

	private static final String PRIVATE_KEY = "HwJx5gW/bGwaxyJNfx1uq+Jg1NpyYCdVGXvd7incVjI=";
	private static final String APPLICATION_ID = "601ce90d5b2948002151fb7c";

	static String GetToken() throws BootpayException {
		JSONObject data = new JSONObject();
		data.put("application_id", APPLICATION_ID);
		data.put("private_key", PRIVATE_KEY);
		HttpResponse<JsonNode> res = Unirest
			.post("https://api.bootpay.co.kr/request/token")
			.header("Content-Type", "application/json")
			.body(data.toString())
			.asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			return json.getJSONObject("data").getString("token");
		} else {
			throw new BootpayException("토큰을 가져오는데 실패하엿습니다.");
		}
	}

	/*
    {
    "status": 200,
    "code": 0,
    "message": "",
    "data": {
        "receipt_id": "6029cd90d8c1bd001ef759bc",
        "order_id": "1234_1234",
        "name": "마스카라",
        "item_name": "마스카라",
        "price": 1000,
        "tax_free": 0,
        "remain_price": 1000,
        "remain_tax_free": 0,
        "cancelled_price": 0,
        "cancelled_tax_free": 0,
        "receipt_url": "https://app.bootpay.co.kr/bill/VG5OZ0w1NkJ2dGxmS0Rrc2dRNkJwU1BBUVQ2cHNJSXFpVGZPb3Rta1VwR3Rx%0AZz09LS1vUnE5bEZJYiswWHhlSjBKLS0wRnNQcForNVIxQ2tYV3FvR3NCY3dn%0APT0%3D%0A",
        "unit": "krw",
        "pg": "welcome",
        "method": "card",
        "pg_name": "웰컴페이먼츠",
        "method_name": "카드결제",
        "payment_data": {
            "card_name": "BC카드",
            "card_no": "465887752371",
            "card_quota": "00",
            "card_code": "11",
            "card_auth_no": "27632568",
            "receipt_id": "6029cd90d8c1bd001ef759bc",
            "n": "마스카라",
            "p": 1000,
            "tid": "WPCMX_CARDwelcometst20210215102559966897",
            "pg": "웰컴페이먼츠",
            "pm": "카드결제",
            "pg_a": "welcome",
            "pm_a": "card",
            "o_id": "1234_1234",
            "p_at": "2021-02-15 10:26:00",
            "s": 1,
            "g": 2
        },
        "requested_at": "2021-02-15 10:25:36",
        "purchased_at": "2021-02-15 10:26:00",
        "status": 1,
        "status_en": "complete",
        "status_ko": "결제완료"
    }
}
    */
	public static JSONObject ValidateReceipt(String receipt_id) throws BootpayException {
		String token = GetToken();
		HttpResponse<JsonNode> res = Unirest.get("https://api.bootpay.co.kr/receipt/" + receipt_id).header("Authorization", token).asJson();

		if (res.getStatus() == 200) {
			JSONObject json = res.getBody().getObject();
			if (InsuStringUtil.Equals(json.getString("status"), "200")) {
				return json.getJSONObject("data");
			} else {
				log.error("ValidateReceipt:{}", json.toString());
				throw new BootpayException("영수증 검증에 문제가 발생하였습니다." + json.getString("message"));
			}
		} else {
			throw new BootpayException("영수증 검증에 문제가 발생하였습니다.");
		}
	}
}
