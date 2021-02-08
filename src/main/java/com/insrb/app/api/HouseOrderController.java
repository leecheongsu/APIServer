package com.insrb.app.api;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import com.insrb.app.exception.AuthException;
import com.insrb.app.exception.AuthExpiredException;
import com.insrb.app.exception.EncryptException;
import com.insrb.app.mapper.IN003TMapper;
import com.insrb.app.mapper.IN011TMapper;
import com.insrb.app.mapper.IN007TMapper;
import com.insrb.app.util.Authentication;
import com.insrb.app.util.InsuDateUtil;
import com.insrb.app.util.cyper.UserInfoCyper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/house/orders")
public class HouseOrderController {

	@Autowired
	IN003TMapper in003tMapper;

	@Autowired
	IN007TMapper in007tMapper;

	@Autowired
	IN011TMapper termsMapper;

	@GetMapping(path = "/{quote_no}")
	public Map<String, Object> selectById(
		@PathVariable String quote_no,
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@RequestParam(name = "user_id", required = true) String user_id
	) {
		try {
			Authentication.ValidateAuthHeader(auth_header, user_id);

			Map<String, Object> order = in003tMapper.selectById(quote_no);
			if (Objects.isNull(order)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			return order;
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (AuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@PostMapping(path = "")
	@ResponseStatus(HttpStatus.OK)
	public String insert(
		@RequestBody(required = true) Map<String, Object> body,
		@RequestHeader(name = "Authorization", required = false) String auth_header
	) {
		String user_id = (String) body.get("user_id");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		log.info(data.toString());
		try {
			Authentication.ValidateAuthHeader(auth_header, user_id);
			String quote_no = (String) data.get("quote_no");
			String prod_code = (String) data.get("prod_code");
			int opayment = (int) data.get("opayment");
			String polholder = (String) data.get("polholder");
			String insurant_a = (String) data.get("insurant_a");
			String insurant_b = (String) data.get("insurant_b");
			int premium = (int) data.get("premium");
			String insdate = (String) data.get("insdate");
			Date ins_from = InsuDateUtil.ToDate((String) data.get("ins_from"));
			Date ins_to = InsuDateUtil.ToDate((String) data.get("ins_to"));
			int ptype = (int) data.get("ptype");
			String insloc = (String) data.get("insloc");
			String mobile = (String) data.get("mobile");
			String email = (String) data.get("email");
			String pbohumja_mobile = (String) data.get("pbohumja_mobile");
			String enc_pbohumja_mobile = UserInfoCyper.EncryptMobile(pbohumja_mobile);

			String jumin = (String) data.get("jumin");
			String encJuminb = UserInfoCyper.EncryptJuminb(jumin);

			// String user_id = (String) data.get("user_id");
			int o_by = (int) data.get("o_by");
			String owner = (String) data.get("owner");
			String pbohumja_birth = (String) data.get("pbohumja_birth");
			int advisor_no = (int) data.get("advisor_no");
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
				enc_pbohumja_mobile,
				encJuminb,
				user_id,
				o_by,
				owner,
				pbohumja_birth,
				advisor_no
			);
			return quote_no;
		} catch (ParseException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다.");
		} catch (EncryptException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화에 문제 있습니다.");
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (AuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	// /api/house/orders/[order_id]/phone-certification
	@PostMapping(path = "/{quote_no}/phone-certification")
	@ResponseStatus(HttpStatus.OK)
	public String phone_certification(
		@PathVariable String quote_no,
		@RequestBody(required = true) Map<String, Object> body,
		@RequestHeader(name = "Authorization", required = false) String auth_header
	) {
		String user_id = (String) body.get("user_id");
		try {
			Authentication.ValidateAuthHeader(auth_header, user_id);
			Map<String, Object> data = (Map<String, Object>) body.get("data");
			log.info(data.toString());
			String rslt_name = (String) data.get("rslt_name");
			String rslt_birthday = (String) data.get("rslt_birthday");
			String rslt_sex_cd = (String) data.get("rslt_sex_cd");
			String rslt_ntv_frnr_cd = (String) data.get("rslt_ntv_frnr_cd");
			String di = (String) data.get("di");
			String ci = (String) data.get("ci");
			String ci_update = (String) data.get("ci_update");
			String tel_com_cd = (String) data.get("tel_com_cd");
			String tel_no = (String) data.get("tel_no");
			String return_msg = quote_no;

			in007tMapper.delete(return_msg);
			in007tMapper.insert(
				rslt_name,
				rslt_birthday,
				rslt_sex_cd,
				rslt_ntv_frnr_cd,
				di,
				ci,
				ci_update,
				tel_com_cd,
				tel_no,
				return_msg
			);
			return quote_no;
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (AuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@PostMapping(path = "/{quote_no}/terms")
	@ResponseStatus(HttpStatus.OK)
	public String terms(
		@PathVariable String quote_no,
		@RequestBody(required = true) Map<String, Object> body,
		@RequestHeader(name = "Authorization", required = false) String auth_header
	) {
		String user_id = (String) body.get("user_id");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		log.info(data.toString());
		try {
			Authentication.ValidateAuthHeader(auth_header, user_id);

			int termsA_1 = (int) data.get("termsA_1");
			int termsA_2 = (int) data.get("termsA_2");
			int termsA_3 = (int) data.get("termsA_3");
			int termsA_4 = (int) data.get("termsA_4");
			int termsA_5 = (int) data.get("termsA_5");
			int termsB_1 = (int) data.get("termsB_1");
			int termsB_2 = (int) data.get("termsB_2");
			int termsB_3 = (int) data.get("termsB_3");
			int termsC_1 = (int) data.get("termsC_1");
			int termsC_2 = (int) data.get("termsC_2");
			int termsC_3 = (int) data.get("termsC_3");
			int termsC_4 = (int) data.get("termsC_4");
			int termsC_5 = (int) data.get("termsC_5");
			int termsD_1 = (int) data.get("termsD_1");
			int termsD_2 = (int) data.get("termsD_2");
			int termsD_3 = (int) data.get("termsD_3");
			int termsE_1 = (int) data.get("termsE_1");
			int termsE_2 = (int) data.get("termsE_2");
			int termsE_3 = (int) data.get("termsE_3");
			int termsF_1 = (int) data.get("termsF_1");
			int termsG_1 = (int) data.get("termsG_1");

			termsMapper.delete(quote_no);
			termsMapper.insert(
				user_id,
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
			return quote_no;
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (AuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}
}
