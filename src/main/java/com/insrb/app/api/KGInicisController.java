package com.insrb.app.api;

import com.insrb.app.exception.KGInicisException;
import com.insrb.app.mapper.IN003TMapper;
import com.insrb.app.mapper.IN009TMapper;
import com.insrb.app.util.InsuDateUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.KakaoMessageUtil;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// ref: https://manual.inicis.com/mobile/ 참고

@Slf4j
@RestController
@RequestMapping("/kginicis")
public class KGInicisController {

	@Value("${kginicis.rtn.server}")
	private String kginicis_rtn_server;

	@Autowired
	IN003TMapper in003tMapper;

	@Autowired
	IN009TMapper in009tMapper;

	@GetMapping(path = "/vacct/param")
	public Map<String, Object> vacct_param() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("P_MID", "insurobo01");
		data.put("P_INI_PAYMENT", "VBANK");
		data.put("P_CHARSET", "utf-8");
		data.put("P_VBANK_DT", InsuDateUtil.ToChar(new Date(), "yyyyMMdd"));
		data.put("P_VBANK_TM", "2359");
		data.put("P_HPP_METHOD", "1"); //휴대폰결제 필수 [1:컨텐츠, 2:실물]
		data.put("P_NEXT_URL", kginicis_rtn_server + "/kginicis/rtn/house");
		data.put("P_NOTI_URL", kginicis_rtn_server + "/kginicis/noti/house");
		return data;
	}

	// 아래 URL은 Filterconfig에서 Rule Out되어야 함.
	// @PostMapping(path = "/rtn/house")

	@RequestMapping(path = "/rtn/house", method = { RequestMethod.POST, RequestMethod.GET }, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String rtn_house(
		@RequestParam(name = "P_STATUS", required = false) String P_STATUS,
		@RequestParam(name = "P_RMESG1", required = false) String P_RMESG1,
		@RequestParam(name = "P_TID", required = false) String P_TID,
		@RequestParam(name = "P_REQ_URL", required = false) String P_REQ_URL,
		@RequestParam(name = "P_NOTI", required = false) String P_NOTI
	) {
		HttpResponse<String> res = Unirest
			.get(P_REQ_URL)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.queryString("P_TID", P_TID)
			.queryString("P_MID", "insurobo01")
			.asString();
		String rtn_string = res.getBody();
		log.info("Rtn: {}", rtn_string);
		String[] values = rtn_string.split("&");

		JSONObject status = new JSONObject();
		JSONObject data = new JSONObject();
		if (res.getStatus() == 200) {
			if (InsuStringUtil.Equals(P_STATUS, "00")) {
				// 파라미터 형식(A=B&C=D&E=F ....)으로 오는 값을 분해한다.
				status.put("status", "ok");
				for (int x = 0; x < values.length; x++) {
					String[] key_value = values[x].split("=");
					log.info(values[x], key_value.length); // 승인결과를 출력
					if (key_value.length == 2) {
						// log.info("K:V: {}:{}", key_value[0], key_value[1]);
						data.put(key_value[0], key_value[1]);
					} else if (key_value.length == 1) {
						data.put(key_value[0], "");
					}
				}
			} else {
				status.put("status", "fail");
				data.put("message", P_RMESG1);
			}
		} else {
			status.put("status", "fail");
			data.put("message", "이니시스 연결 오류.");
		}
		status.put("data", data);
		// 성공 시 클라이언트에 보낼 메세지
		String html =
			"<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('" + status.toString() + "');</script></html>";
		log.info("html:{}", html);
		return html;
	}

	// 아래 URL은 Filterconfig에서 Rule Out되어야 함.
	// @GetMapping(path = "/noti/house")
	@RequestMapping(path = "/noti/house", method = { RequestMethod.POST, RequestMethod.GET }, produces = MediaType.TEXT_HTML_VALUE)
	public String noti_house(
		@RequestParam(name = "P_STATUS", required = false) String P_STATUS, //거래상태 [00:가상계좌 채번, 02:가상계좌입금통보]
		@RequestParam(name = "P_TID", required = false) String P_TID, //승인거래번호
		@RequestParam(name = "P_RMESG1", required = false) String P_RMESG1, //메시지1 [채번된 가상계좌번호|입금기한]
		@RequestParam(name = "P_TYPE", required = false) String P_TYPE, //지불수단
		@RequestParam(name = "P_AUTH_DT", required = false) String P_AUTH_DT, //승인일자 [YYYYMMDDhhmmss]
		@RequestParam(name = "P_MID", required = false) String P_MID, //상점아이디
		@RequestParam(name = "P_OID", required = false) String P_OID, //주문번호
		@RequestParam(name = "P_AMT", required = false) String P_AMT, //거래금액
		@RequestParam(name = "P_FN_CD1", required = false) String P_FN_CD1, //은행코드
		@RequestParam(name = "P_FN_NM", required = false) String P_FN_NM, //입금은행명
		@RequestParam(name = "P_NOTI", required = false) String P_NOTI //가맹점 임의 데이터
	)
		throws UnsupportedEncodingException {
		// log.info("P_STATUS: {}", P_STATUS);
		// log.info("P_RMESG1: {}", P_RMESG1);
		// log.info("P_TID: {}", P_TID);
		// log.info("P_TYPE: {}", P_TYPE);
		// log.info("P_AUTH_DT: {}", P_AUTH_DT);
		// log.info("P_MID: {}", P_MID);
		// log.info("P_OID: {}", P_OID);
		// log.info("P_AMT: {}", P_AMT);
		// log.info("P_FN_CD1: {}", P_FN_CD1);
		// log.info("P_FN_NM: {}", P_FN_NM);
		// log.info("P_NOTI: {}", P_NOTI);
		if (!Objects.isNull(P_TID) && !Objects.isNull(P_STATUS) && !Objects.isNull(P_OID)) {
			String quote_no = P_OID;
			String tid = P_TID;
			int price = (int) InsuStringUtil.ToIntOrDefault(P_AMT, 0);
			if (InsuStringUtil.Equals(P_STATUS, "02")) { //가상계좌 입금
				in009tMapper.delete(tid, P_STATUS);
				in009tMapper.insertVacct(quote_no, tid, P_STATUS, P_RMESG1, P_AUTH_DT, P_FN_CD1, price);
				try {
					sendAI001KakaoMessage(quote_no);
				} catch (KGInicisException e) {
					throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
				}
				return "OK";
			}
		}

		throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "입금 Noti from KG이니시스 오류");
	}

	private void sendAI001KakaoMessage(String quote_no) throws KGInicisException {
		Map<String, Object> order = in003tMapper.selectById(quote_no);
		log.info("order", order.toString());
		if (Objects.isNull(order) || order.size() == 0) throw new KGInicisException("입금처리할 주문이 없읍니다.");
		KakaoMessageUtil.AI001(
			(String) order.get("mobile"),
			(String) order.get("polholder"),
			(String) order.get("p_name"), //order.get("p_name"), //상품명
			(String) order.get("insurant_a"),
			(String) order.get("insloc"),
			String.valueOf(order.get("amt_ins")),
			String.valueOf(order.get("premium")),
			(String) order.get("quote_no"),
			InsuDateUtil.ToChar((Date) order.get("insdate"), "yyyy.MM.dd"),
			InsuDateUtil.ToChar((Date) order.get("ins_from"), "yyyy.MM.dd"),
			InsuDateUtil.ToChar((Date) order.get("ins_from"), "yyyy.MM.dd") +
			" 24:00 ~ " +
			InsuDateUtil.ToChar((Date) order.get("ins_to"), "yyyy.MM.dd") +
			" 24:00"
		);
	}
}
