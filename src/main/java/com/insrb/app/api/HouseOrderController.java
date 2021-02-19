package com.insrb.app.api;

import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuAuthExpiredException;
import com.insrb.app.exception.InsuEncryptException;
import com.insrb.app.exception.KGInisisException;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN003TMapper;
import com.insrb.app.mapper.IN007TMapper;
import com.insrb.app.mapper.IN009TMapper;
import com.insrb.app.mapper.IN011TMapper;
import com.insrb.app.util.InsuAuthentication;
import com.insrb.app.util.InsuDateUtil;
import com.insrb.app.util.InsuNumberUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.KGInisisUtil;
import com.insrb.app.util.KakaoMessageUtil;
import com.insrb.app.util.cyper.UserInfoCyper;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

	//카드결제
	@PostMapping(path = "/card")
	@ResponseStatus(HttpStatus.OK)
	public String card(
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
			String prod_name = (String) data.get("prod_name"); // 인슈로보주택종합보험(메리츠화재)
			String polholder = (String) data.get("polholder");
			int premium = (int) data.get("premium");
			String mobile = (String) data.get("mobile");
			Map<String, Object> terms = (Map<String, Object>) data.get("terms");
			List<Map<String, Object>> premiums = (List<Map<String, Object>>) data.get("premiums");
			Map<String, Object> card = (Map<String, Object>) data.get("card");

			String card_number = (String) card.get("card_number");
			String card_expire = (String) card.get("card_expire");
			String reg_no = (String) card.get("reg_no");
			String card_pw = (String) card.get("card_pw");
			try {
				JSONObject kginisis_json = KGInisisUtil.card(
					quote_no,
					prod_name,
					polholder,
					user_id,
					mobile,
					String.valueOf(premium),
					card_number,
					card_expire,
					reg_no,
					card_pw
				);
				// log.info("terms:{}", terms.toString());
				// log.info("premiums:{}", premiums.toString());
				// 카드결제 결과 저장
				insertIN009T(quote_no, kginisis_json);
				insertOrder(quote_no, data);
				insertTerms(quote_no, terms);
				updatePremiums(quote_no, premiums);
				sendA001KakaoMessage(quote_no, data);
			} catch (KGInisisException e) {
				throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
			}

			return quote_no;
		} catch (ParseException e) {
			log.error("/house/orders/card: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다.");
		} catch (InsuEncryptException e) {
			log.error("/house/orders/card: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화에 문제 있습니다.");
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	//가상계좌결제
	@PostMapping(path = "/vacct")
	@ResponseStatus(HttpStatus.OK)
	public String vacct(
		@RequestBody(required = true) Map<String, Object> body,
		@RequestHeader(name = "Authorization", required = false) String auth_header
	) {
		String user_id = (String) body.get("user_id");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		// log.info("User:{}", user_id);
		log.info("/vacct: {}", data.toString());
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);
			String quote_no = (String) data.get("quote_no");
			Map<String, Object> terms = (Map<String, Object>) data.get("terms");
			List<Map<String, Object>> premiums = (List<Map<String, Object>>) data.get("premiums");

			log.info("terms:{}", terms.toString());
			log.info("premiums:{}", premiums.toString());
			insertOrder(quote_no, data);
			insertTerms(quote_no, terms);
			updatePremiums(quote_no, premiums);
			sendA002KakaoMessage(quote_no, data);

			return quote_no;
		} catch (ParseException e) {
			log.error("/house/orders/vacct: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다.");
		} catch (InsuEncryptException e) {
			log.error("/house/orders/vacct: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화에 문제 있습니다.");
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	private void insertIN009T(String quote_no, JSONObject json) {
		String tid = json.getString("tid");
		String resultcode = json.getString("resultCode");
		String resultmsg = json.getString("resultMsg");
		String paydate = json.getString("payDate");
		String paytime = json.getString("payTime");
		String payauthcode = json.getString("payAuthCode");
		String cardcode = json.getString("cardCode");
		String checkflg = json.getString("checkFlg");
		String payauthquota = json.getString("payAuthQuota");
		String prtccode = json.getString("prtcCode");
		int price = json.getInt("price");

		in009tMapper.insertCard(
			quote_no,
			tid,
			resultcode,
			resultmsg,
			paydate,
			paytime,
			payauthcode,
			cardcode,
			checkflg,
			payauthquota,
			prtccode,
			price
		);
	}

	private void insertOrder(String quote_no, Map<String, Object> data) throws ParseException, InsuEncryptException {
		String prod_code = (String) data.get("prod_code"); // 인슈로보주택종합보험(메리츠화재)
		long amt_ins = Long.valueOf(String.valueOf(data.get("amt_ins"))); //보험목적물가액
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
		Map<String, Object> vacct = (Map<String, Object>) data.get("vacct");
		String v_bank_name = "";
		String v_bank_no = "";
		String v_bank_due_date = "";

		if (vacct != null && vacct.size() > 0) {
			v_bank_name = (String) vacct.get("v_bank_name");
			v_bank_no = (String) vacct.get("v_bank_no");
			v_bank_due_date = (String) vacct.get("v_bank_due_date");
		}

		in003tMapper.delete(quote_no);
		in003tMapper.insert(
			quote_no,
			prod_code,
			amt_ins,
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
			v_bank_name,
			v_bank_no,
			v_bank_due_date,
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

	private void sendA001KakaoMessage(String quote_no, Map<String, Object> data) throws ParseException {
		String prod_name = (String) data.get("prod_name"); // 인슈로보주택종합보험(메리츠화재)
		int amt_ins = (int) data.get("amt_ins");
		int premium = (int) data.get("premium");
		String mobile = (String) data.get("mobile");
		String polholder = (String) data.get("polholder");
		String insloc = (String) data.get("insloc");
		String insurant_a = (String) data.get("insurant_a");
		Date insdate = InsuDateUtil.ToDate((String) data.get("insdate"));
		Date ins_from = InsuDateUtil.ToDate((String) data.get("ins_from"));
		Date ins_to = InsuDateUtil.ToDate((String) data.get("ins_to"));

		KakaoMessageUtil.A001(
			mobile,
			polholder,
			prod_name,
			insurant_a,
			insloc,
			InsuNumberUtil.ToChar(amt_ins),
			InsuNumberUtil.ToChar(premium),
			quote_no,
			InsuDateUtil.ToChar(insdate, "yyyy.MM.dd"),
			InsuDateUtil.ToChar(ins_from, "yyyy.MM.dd"),
			InsuDateUtil.ToChar(ins_from, "yyyy.MM.dd") + " 24:00 ~ " + InsuDateUtil.ToChar(ins_to, "yyyy.MM.dd") + " 24:00"
		);
	}

	private void sendA002KakaoMessage(String quote_no, Map<String, Object> data) throws ParseException {
		String prod_name = (String) data.get("prod_name"); // 인슈로보주택종합보험(메리츠화재)
		int premium = (int) data.get("premium");
		String mobile = (String) data.get("mobile");
		String polholder = (String) data.get("polholder");

		String v_bank_name = "";
		String v_bank_no = "";
		Date v_bank_due_date = null;

		Map<String, Object> vacct = (Map<String, Object>) data.get("vacct");
		if (vacct != null && vacct.size() > 0) {
			v_bank_name = (String) vacct.get("v_bank_name");
			v_bank_no = (String) vacct.get("v_bank_no");
			v_bank_due_date = InsuDateUtil.ToDateTime((String) vacct.get("v_bank_due_date"));
		}
		KakaoMessageUtil.A002(
			mobile,
			polholder,
			prod_name,
			v_bank_name,
			v_bank_no,
			InsuNumberUtil.ToChar(premium),
			InsuDateUtil.ToChar(v_bank_due_date, "yyyy년MM월dd일 HH시mm분ss초")
		);
	}
}
