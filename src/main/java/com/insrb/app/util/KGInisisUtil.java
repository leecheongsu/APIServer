package com.insrb.app.util;

import com.insrb.app.exception.KGInisisException;
import com.insrb.app.util.cyper.AES256Ciper;
import com.insrb.app.util.cyper.SHA512Util;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Slf4j
public class KGInisisUtil {

	static String KEY = "Igx2ofmG1L0BsWQ1";
	static String IV = "blVTWJdw9IzrYw==";

	/*
	{
  "tid": "INIAPICARDinsurobo0220210216165408286836",
  "resultCode": "00",
  "resultMsg": "[Card|Transaction has been successful]",
  "payDate": "20210216",
  "payTime": "165408",
  "payAuthCode": "56528778",
  "cardCode": "11",
  "checkFlg": "0",
  "payAuthQuota": "00",
  "prtcCode": "1",
  "price": "1000",
  "cardName": "BC카드",
  "cardPoint": "",
  "usePoint": "",
  "generalEvent": "",
  "quotainterest": "",
  "buyCode": "",
  "detailCode": "00"
}
	 */
	public static JSONObject card(
		String moid,
		String goodName,
		String buyerName,
		String buyerEmail,
		String buyerTel,
		String price,
		String plainCardNumber,
		String plainCardExpire,
		String plainRegNo,
		String plainCardPw
	)
		throws KGInisisException {
		try {
			AES256Ciper aes = new AES256Ciper(KEY, IV);
			String type = "Pay"; // "Pay" 고정
			String paymethod = "Card"; // "Card" 고정
			String timestamp = InsuDateUtil.GetTimestampString(new Date()); // 전문생성시간 [YYYYMMDDhhmmss]
			String clientIp = "insrb.com"; // 가맹점 요청 서버IP (추후 거래 확인 등에 사용됨)
			String mid = "insurobo02"; // 상점아이디
			String url = "insrb.com"; // 가맹점 URL
			// String moid = "Q20210215194229866"; // 가맹점주문번호
			// String goodName = "인슈로보주택종합보험"; // 상품명
			// String buyerName = "인슈로보";
			// String buyerEmail = "vingorius@gmail.com"; // 구매자 이메일주소
			// String buyerTel = "01047017956"; // 구매자 휴대폰번호
			// String price = "1000"; // 결제금액
			String cardQuota = "00"; // 할부기간 ["00":일시불, 그 외 : 02, 03 ...]
			String quotaInterest = ""; // 무이자구분 ["1":무이자]
			String currency = "WON"; // 통화코드 [WON,USD]
			String authentification = "OO"; // 본인인증 여부 ["00" 고정]
			String cardNumber = aes.encode(plainCardNumber); // 카드번호   ENC
			String cardExpire = aes.encode(plainCardExpire); // 카드유효기간 [YYMM]   ENC
			String regNo = aes.encode(plainRegNo); // 생년월일 [YYYYMMDD]/사업자번호   ENC
			String cardPw = aes.encode(plainCardPw); // 카드비밀번호 앞 2자리   ENC
			String cardPoint = "2"; // 카드포인트 사용유무 ["1":사용, "2":미사용]
			String language = ""; // 언어설정 [ENG] * 결과메세지 언어 셋팅
			String plain = KEY + type + paymethod + timestamp + clientIp + mid + moid + price + cardNumber;
			String hashData = SHA512Util.Hash(plain); // 전문위변조 HASH   HASH hash(KEY+type+paymethod+timestamp+c
			log.debug("plain:{}", plain);
			log.debug("hashData:{}", hashData);
			HttpResponse<JsonNode> res = Unirest
				.post("https://iniapi.inicis.com/api/v1/formpay")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.field("type", type)
				.field("paymethod", paymethod)
				.field("timestamp", timestamp)
				.field("clientIp", clientIp)
				.field("mid", mid)
				.field("url", url)
				.field("moid", moid)
				.field("goodName", goodName)
				.field("buyerName", buyerName)
				.field("buyerEmail", buyerEmail)
				.field("buyerTel", buyerTel)
				.field("price", price)
				.field("cardQuota", cardQuota)
				.field("quotaInterest", quotaInterest)
				.field("currency", currency)
				.field("authentification", authentification)
				.field("cardNumber", cardNumber)
				.field("cardExpire", cardExpire)
				.field("regNo", regNo)
				.field("cardPw", cardPw)
				.field("cardPoint", cardPoint)
				.field("language", language)
				.field("hashData", hashData)
				.asJson();
			JSONObject json = res.getBody().getObject();
			if (!InsuStringUtil.Equals(json.getString("resultCode"), "00")) {
				throw new KGInisisException("결제 오류: " + json.getString("resultCode") + ", " + json.getString("resultMsg"));
			}
			return json;
		} catch (
			InvalidKeyException
			| UnsupportedEncodingException
			| NoSuchAlgorithmException
			| NoSuchPaddingException
			| InvalidAlgorithmParameterException
			| IllegalBlockSizeException
			| BadPaddingException e
		) {
			throw new KGInisisException("결재 오류:암호화");
		}
	}

	// public static String card_mock()
	// 	throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
	// 	String KEY = "Igx2ofmG1L0BsWQ1";
	// 	String IV = "blVTWJdw9IzrYw==";
	// 	AES256Ciper aes = new AES256Ciper(KEY, IV);
	// 	String type = "Pay"; // "Pay" 고정
	// 	String paymethod = "Card"; // "Card" 고정
	// 	String timestamp = InsuDateUtil.GetTimestampString(new Date()); // 전문생성시간 [YYYYMMDDhhmmss]
	// 	String clientIp = "insrb.com"; // 가맹점 요청 서버IP (추후 거래 확인 등에 사용됨)
	// 	String mid = "insurobo02"; // 상점아이디
	// 	String url = "insrb.com"; // 가맹점 URL
	// 	String moid = "Q20210215194229866"; // 가맹점주문번호
	// 	String goodName = "인슈로보주택종합보험"; // 상품명
	// 	String buyerName = "인슈로보";
	// 	String buyerEmail = "vingorius@gmail.com"; // 구매자 이메일주소
	// 	String buyerTel = "01047017956"; // 구매자 휴대폰번호
	// 	String price = "1000"; // 결제금액
	// 	String cardQuota = "00"; // 할부기간 ["00":일시불, 그 외 : 02, 03 ...]
	// 	String quotaInterest = ""; // 무이자구분 ["1":무이자]
	// 	String currency = "WON"; // 통화코드 [WON,USD]
	// 	String authentification = "OO"; // 본인인증 여부 ["00" 고정]
	// 	String cardNumber = aes.encode("4171030633939931"); // 카드번호   ENC
	// 	String cardExpire = aes.encode("2403"); // 카드유효기간 [YYMM]   ENC
	// 	String regNo = aes.encode("19681112"); // 생년월일 [YYYYMMDD]/사업자번호   ENC
	// 	String cardPw = aes.encode("30"); // 카드비밀번호 앞 2자리   ENC
	// 	String cardPoint = "2"; // 카드포인트 사용유무 ["1":사용, "2":미사용]
	// 	String language = ""; // 언어설정 [ENG] * 결과메세지 언어 셋팅
	// 	String plain = KEY + type + paymethod + timestamp + clientIp + mid + moid + price + cardNumber;
	// 	String hashData = SHA512Util.Hash(plain); // 전문위변조 HASH   HASH hash(KEY+type+paymethod+timestamp+c
	// 	log.debug("plain:{}", plain);
	// 	log.debug("hashData:{}", hashData);
	// 	HttpResponse<JsonNode> res = Unirest
	// 		.post("https://iniapi.inicis.com/api/v1/formpay")
	// 		.header("Content-Type", "application/x-www-form-urlencoded")
	// 		.field("type", type)
	// 		.field("paymethod", paymethod)
	// 		.field("timestamp", timestamp)
	// 		.field("clientIp", clientIp)
	// 		.field("mid", mid)
	// 		.field("url", url)
	// 		.field("moid", moid)
	// 		.field("goodName", goodName)
	// 		.field("buyerName", buyerName)
	// 		.field("buyerEmail", buyerEmail)
	// 		.field("buyerTel", buyerTel)
	// 		.field("price", price)
	// 		.field("cardQuota", cardQuota)
	// 		.field("quotaInterest", quotaInterest)
	// 		.field("currency", currency)
	// 		.field("authentification", authentification)
	// 		.field("cardNumber", cardNumber)
	// 		.field("cardExpire", cardExpire)
	// 		.field("regNo", regNo)
	// 		.field("cardPw", cardPw)
	// 		.field("cardPoint", cardPoint)
	// 		.field("language", language)
	// 		.field("hashData", hashData)
	// 		.asJson();
	// 	JSONObject json = res.getBody().getObject();
	// 	log.debug(json.toString());
	// 	return json.toString();
	// }

	// public static String vacct_mock() {
	// 	String type = "Pay"; // "Pay" 고정
	// 	String paymethod = "Vacct"; // "Vacct" 고정
	// 	String timestamp = InsuDateUtil.GetTimestampString(new Date()); // 전문생성시간 [YYYYMMDDhhmmss]
	// 	String clientIp = "insrb.com"; // 가맹점 요청 서버IP (추후 거래 확인 등에 사용됨)
	// 	String mid = "insurobo01"; // 상점아이디
	// 	String url = "insrb.com"; // 가맹점 URL
	// 	String moid = "Q20210215194229866"; // 가맹점주문번호
	// 	String goodName = "인슈로보주택종합보험"; // 상품명
	// 	String buyerName = "인슈로보"; // 구매자명
	// 	String buyerEmail = "vingorius@gmail.com"; // 구매자 이메일주소
	// 	String buyerTel = "01047017956"; // 구매자 휴대폰번호
	// 	String price = "1100"; // 거래금액
	// 	String currency = "WON"; // 통화코드 [WON,USD]
	// 	String bankCode = "20"; // 은행코드   Code, 20:우리은행
	// 	String dtInput = ""; // 입금예정일자 [YYYYMMDD]
	// 	String tmInput = ""; // 입금예정시간 [hhmm]
	// 	String nmInput = ""; // 입금자명
	// 	String flgCash = ""; // 현금영수증 발행여부 ["0":미발행, "1":소득공제 발행, "2":지출증빙]
	// 	String cashRegNo = ""; // 현금영수증 발행정보 (주민번호, 휴대폰번호, 사업장등록번호 등)   ENC
	// 	String vacctType = ""; // 타입 ["3" 과오납체크] * 과오납 체크의 경우만 세팅
	// 	String vacct = ""; // 벌크가상계좌번호 * 과오납 체크의 경우만 세팅
	// 	String hashData = ""; // 전문위변조 HASH   HASH hash(KEY+type+paymethod+timestamp+clientIp+mid+moi
	// 	String plain = KEY + type + paymethod + timestamp + clientIp + mid + moid + price;
	// 	log.debug("plain:{}", plain);
	// 	log.debug("hashData:{}", hashData);
	// 	HttpResponse<JsonNode> res = Unirest
	// 		.post("https://iniapi.inicis.com/api/v1/formpay")
	// 		.header("Content-Type", "application/x-www-form-urlencoded")
	// 		.field("type", type)
	// 		.field("paymethod", paymethod)
	// 		.field("timestamp", timestamp)
	// 		.field("clientIp", clientIp)
	// 		.field("mid", mid)
	// 		.field("url", url)
	// 		.field("moid", moid)
	// 		.field("goodName", goodName)
	// 		.field("buyerName", buyerName)
	// 		.field("buyerEmail", buyerEmail)
	// 		.field("buyerTel", buyerTel)
	// 		.field("price", price)
	// 		.field("currency", currency)
	// 		.field("bankCode", bankCode)
	// 		.field("dtInput", dtInput)
	// 		.field("tmInput", tmInput)
	// 		.field("nmInput", nmInput)
	// 		.field("flgCash", flgCash)
	// 		.field("cashRegNo", cashRegNo)
	// 		.field("hashData", hashData)
	// 		.asJson();
	// 	JSONObject json = res.getBody().getObject();
	// 	log.debug(json.toString());
	// 	return json.toString();
	// }
}
