package com.insrb.app.api;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.insrb.app.exception.BootpayException;
import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuAuthExpiredException;
import com.insrb.app.exception.InsuEncryptException;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN003TMapper;
import com.insrb.app.mapper.IN007TMapper;
import com.insrb.app.mapper.IN009TMapper;
import com.insrb.app.mapper.IN011TMapper;
import com.insrb.app.util.BootpayUtil;
import com.insrb.app.util.InsuAuthentication;
import com.insrb.app.util.InsuDateUtil;
import com.insrb.app.util.InsuJsonUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.cyper.UserInfoCyper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/house/orders")
public class HouseOrderController {

	@Autowired
	IN002TMapper in002tMapper;

	@Autowired
	IN003TMapper in003tMapper;

	@Autowired
	IN007TMapper in007tMapper;

	@Autowired
	IN009TMapper in009tMapper;

	@Autowired
	IN011TMapper in011tMapper;

	@PostMapping(path = "")
	@ResponseStatus(HttpStatus.OK)
	public String insert(
		@RequestBody(required = true) Map<String, Object> body,
		@RequestHeader(name = "Authorization", required = false) String auth_header
	) {
		String user_id = (String) body.get("user_id");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		// log.info("User:{}", user_id);
		// log.info(data.toString());
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);
			String quote_no = (String) data.get("quote_no");
			String receipt_id = (String) data.get("receipt_id");
			Map<String, Object> terms = (Map<String, Object>) data.get("terms");
			List<Map<String, Object>> premiums = (List<Map<String, Object>>) data.get("premiums");
			// log.info("terms:{}", terms.toString());
			// log.info("premiums:{}", premiums.toString());
			JSONObject receipt_json = BootpayUtil.ValidateReceipt(receipt_id);
			insertReceipt(quote_no, receipt_json);
			insertOrder(quote_no, data);
			insertTerms(quote_no, terms);
			updatePremiums(quote_no, premiums);

			return quote_no;
		} catch (ParseException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다.");
		} catch (InsuEncryptException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화에 문제 있습니다.");
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		} catch (BootpayException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Bootpay 거래 검증 오류.");
		}
	}

	private void insertReceipt(String quote_no, JSONObject json) {
		String receipt_id = json.getString("receipt_id");
		String pg = json.getString("pg");
		String pg_name = json.getString("pg_name");
		String method = json.getString("method");
		String method_name = json.getString("method_name");
		String name = json.getString("name");
		String order_id = json.getString("order_id");
		String receipt_url = json.getString("receipt_url");
		JSONObject payment_data = json.getJSONObject("payment_data");
		String payment_data_card_name = InsuJsonUtil.IfNullDefault(payment_data,"card_name", "");
		String payment_data_card_no = InsuJsonUtil.IfNullDefault(payment_data,"card_no", "");
		String payment_data_card_quota = InsuJsonUtil.IfNullDefault(payment_data,"card_quota", "");
		String payment_data_card_auth_no = InsuJsonUtil.IfNullDefault(payment_data,"card_auth_no", "");
		String payment_data_vbank_bankname = InsuJsonUtil.IfNullDefault(payment_data,"vbank_bankname", "");
		String payment_data_vbank_accountholder = InsuJsonUtil.IfNullDefault(payment_data,"vbank_accountholder", "");
		String payment_data_vbank_account = InsuJsonUtil.IfNullDefault(payment_data,"vbank_account", "");
		String payment_data_vbank_expiredate = InsuJsonUtil.IfNullDefault(payment_data,"vbank_expiredate", "");
		String payment_data_vbank_username = InsuJsonUtil.IfNullDefault(payment_data,"vbank_username", "");
		String payment_data_vbank_cash_result = InsuJsonUtil.IfNullDefault(payment_data,"vbank_cash_result", "");
		String price = json.getString("price");
		String status = json.getString("status");
		String payment_json = payment_data.toString();
		in009tMapper.insert(
			receipt_id,
			quote_no,
			pg,
			pg_name,
			method,
			method_name,
			name,
			order_id,
			receipt_url,
			payment_data_card_name,
			payment_data_card_no,
			payment_data_card_quota,
			payment_data_card_auth_no,
			payment_data_vbank_bankname,
			payment_data_vbank_accountholder,
			payment_data_vbank_account,
			payment_data_vbank_expiredate,
			payment_data_vbank_username,
			payment_data_vbank_cash_result,
			price,
			status,
			payment_json
		);
	}

	private void insertOrder(String quote_no, Map<String, Object> data) throws ParseException, InsuEncryptException {
		String prod_code = (String) data.get("prod_code"); // 인슈로보주택종합보험(메리츠화재)
		int opayment = (int) data.get("opayment");
		String polholder = (String) data.get("polholder");
		String insurant_a = (String) data.get("insurant_a");
		String insurant_b = (String) data.get("insurant_b");
		int premium = (int) data.get("premium");
		Date insdate = InsuDateUtil.ToDate((String) data.get("insdate"));
		Date ins_from = InsuDateUtil.ToDate((String) data.get("ins_from"));
		Date ins_to = InsuDateUtil.ToDate((String) data.get("ins_to"));
		String ptype = (String) data.get("ptype");
		String insloc = (String) data.get("insloc");
		String mobile = (String) data.get("mobile");
		String email = (String) data.get("email");
		String poption = (String) data.get("poption");
		String receipt_id = (String) data.get("receipt_id");
		String pbohumja_mobile = (String) data.get("pbohumja_mobile");
		String enc_pbohumja_mobile = UserInfoCyper.EncryptMobile(pbohumja_mobile);

		String jumin = (String) data.get("jumin");
		String encJuminb = UserInfoCyper.EncryptJuminb(jumin); // TODO: 주민B만 있으면 sex는 필요없는건가? 왜 저장하지? 그래서 여기선 전체주민번호를 암호화함.

		// String user_id = (String) data.get("user_id");
		// int o_by = (int) data.get("o_by");
		String owner = (String) data.get("owner");
		String pbohumja_birth = (String) data.get("pbohumja_birth");
		String advisor_no = (String) data.get("advisor_no");
		String already_group_ins = (String) data.get("already_group_ins");

		in003tMapper.delete(quote_no);
		in003tMapper.insert(
			quote_no,
			prod_code,
			opayment,
			polholder,
			insurant_a,
			insurant_b,
			premium,
			insdate,
			ins_from,
			ins_to,
			ptype,
			insloc,
			mobile,
			email,
			poption,
			receipt_id,
			enc_pbohumja_mobile, //pbohumja_mobile,
			encJuminb, //jumin,
			owner,
			pbohumja_birth,
			advisor_no,
			already_group_ins
		);
	}

	private void updatePremiums(String quote_no, List<Map<String, Object>> premiums) {
		in002tMapper.updateAplyYnAllToNById(quote_no);
		for (Map<String, Object> premium : premiums) {
			int seq = (int) premium.get("seq");
			in002tMapper.updateAplyYnToYBySeq(quote_no, seq);
		}
	}

	private void insertTerms(String quote_no, Map<String, Object> data) {
		int termsA_1 = InsuStringUtil.ToIntOrDefault(data.get("termsa_1"), 0);
		int termsA_2 = InsuStringUtil.ToIntOrDefault(data.get("termsa_2"), 0);
		int termsA_3 = InsuStringUtil.ToIntOrDefault(data.get("termsa_3"), 0);
		int termsA_4 = InsuStringUtil.ToIntOrDefault(data.get("termsa_4"), 0);
		int termsA_5 = InsuStringUtil.ToIntOrDefault(data.get("termsa_5"), 0);
		int termsB_1 = InsuStringUtil.ToIntOrDefault(data.get("termsb_1"), 0);
		int termsB_2 = InsuStringUtil.ToIntOrDefault(data.get("termsb_2"), 0);
		int termsB_3 = InsuStringUtil.ToIntOrDefault(data.get("termsb_3"), 0);
		int termsC_1 = InsuStringUtil.ToIntOrDefault(data.get("termsc_1"), 0);
		int termsC_2 = InsuStringUtil.ToIntOrDefault(data.get("termsc_2"), 0);
		int termsC_3 = InsuStringUtil.ToIntOrDefault(data.get("termsc_3"), 0);
		int termsC_4 = InsuStringUtil.ToIntOrDefault(data.get("termsc_4"), 0);
		int termsC_5 = InsuStringUtil.ToIntOrDefault(data.get("termsc_5"), 0);
		int termsD_1 = InsuStringUtil.ToIntOrDefault(data.get("termsd_1"), 0);
		int termsD_2 = InsuStringUtil.ToIntOrDefault(data.get("termsd_2"), 0);
		int termsD_3 = InsuStringUtil.ToIntOrDefault(data.get("termsd_3"), 0);
		int termsE_1 = InsuStringUtil.ToIntOrDefault(data.get("termse_1"), 0);
		int termsE_2 = InsuStringUtil.ToIntOrDefault(data.get("termse_2"), 0);
		int termsE_3 = InsuStringUtil.ToIntOrDefault(data.get("termse_3"), 0);
		int termsF_1 = InsuStringUtil.ToIntOrDefault(data.get("termsf_1"), 0);
		int termsG_1 = InsuStringUtil.ToIntOrDefault(data.get("termsf_1"), 0);

		in011tMapper.delete(quote_no);
		in011tMapper.insert(
			quote_no,
			termsA_1,
			termsA_2,
			termsA_3,
			termsA_4,
			termsA_5,
			termsB_1,
			termsB_2,
			termsB_3,
			termsC_1,
			termsC_2,
			termsC_3,
			termsC_4,
			termsC_5,
			termsD_1,
			termsD_2,
			termsD_3,
			termsE_1,
			termsE_2,
			termsE_3,
			termsF_1,
			termsG_1
		);
	}
}
