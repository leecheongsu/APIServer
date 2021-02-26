package com.insrb.app.api;

import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuAuthExpiredException;
import com.insrb.app.exception.InsuEncryptException;
import com.insrb.app.mapper.IN002T_V1Mapper;
import com.insrb.app.mapper.IN003T_V1Mapper;
import com.insrb.app.mapper.IN005CMapper;
import com.insrb.app.mapper.IN005TMapper;
import com.insrb.app.mapper.IN006TMapper;
import com.insrb.app.util.InsuAuthentication;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.cyper.UserInfoCyper;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/users")
// @SuppressWarnings("unchecked")
public class UsersController {

	@Autowired
	IN005TMapper in005tMapper;

	@Autowired
	IN006TMapper in006tMapper;

	@Autowired
	IN005CMapper in005cMapper;

	@Autowired
	IN002T_V1Mapper in002t_v1Mapper;

	@Autowired
	IN003T_V1Mapper in003t_v1Mapper;

	@GetMapping(path = "today")
	public String today() {
		log.debug("Today is called");
		return in005tMapper.getCurrentDateTime();
	}

	// Example For procedure's cursor
	// @GetMapping(path = "/all")
	// public List<Map<String, Object>> selectAll() {
	// Map<String, Object> param = new Map<String, Object>();
	// in005tMapper.selectAll(param);
	// return (List<Map<String, Object>>) param.get("p_cursor");
	// }

	@PostMapping(path = "")
	public String insert(
		@RequestParam(name = "email", required = true) String email,
		@RequestParam(name = "name", required = true) String name,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile,
		@RequestParam(name = "pwd", required = true) String pwd,
		@RequestParam(name = "jumina", required = true) String jumina,
		@RequestParam(name = "sex", required = true) String sex
	) {
		String utype = "u";
		try {
			Integer.parseInt(jumina); // isValidNumber
			Integer.parseInt(mobile); // isValidNumber
			Integer.parseInt(sex); // isValidNumber
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			String encPwd = UserInfoCyper.EncryptPassword(email, pwd);
			int result = in005tMapper.insert(email, name, teltype, encMobile, encPwd, jumina, sex, utype);
			if (result < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "잘못된 주민번호 뒷자리입니다.");
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화 오류.");
		} catch (DataAccessException e) {
			if (e.getRootCause() instanceof SQLException) {
				SQLException sqlEx = (SQLException) e.getRootCause();
				int sqlErrorCode = sqlEx.getErrorCode();
				log.error("sqlErrorCode:" + sqlErrorCode);
				if (sqlErrorCode == -10007) { // Unique Constriant Error
					throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 사용자입니다.");
				}
			}
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
	}

	@PostMapping(path = "business")
	public void business(
		@RequestParam(name = "email", required = true) String email,
		@RequestParam(name = "name", required = true) String name,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile,
		@RequestParam(name = "pwd", required = true) String pwd,
		@RequestParam(name = "jumina", required = true) String jumina,
		@RequestParam(name = "sex", required = true) String sex,
		@RequestParam(name = "comname", required = true) String comname,
		@RequestParam(name = "sosok", required = true) String sosok,
		@RequestParam(name = "businessnum", required = true) String businessnum
	) {
		String utype = "GA";
		try {
			Integer.parseInt(jumina); // isValidNumber
			Integer.parseInt(mobile); // isValidNumber
			Integer.parseInt(sex); // isValidNumber
			Integer.parseInt(businessnum); // isValidNumber
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			String encPwd = UserInfoCyper.EncryptPassword(email, pwd);
			int result = in005tMapper.insert(email, name, teltype, encMobile, encPwd, jumina, sex, utype);
			if (result < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
			in006tMapper.merge(email, comname, sosok, businessnum);
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "주민번호/성별/모바일번호/사업자번호는 숫자여야합니다.");
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화 오류.");
		} catch (DataAccessException e) {
			if (e.getRootCause() instanceof SQLException) {
				SQLException sqlEx = (SQLException) e.getRootCause();
				int sqlErrorCode = sqlEx.getErrorCode();
				log.error("sqlErrorCode:" + sqlErrorCode);
				if (sqlErrorCode == -10007) { // Unique Constriant Error
					throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 사용자입니다.");
				}
			}
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
		}
	}

	@GetMapping(path = "/advisors")
	public List<Map<String, Object>> advisors() {
		List<Map<String, Object>> advisors = in005cMapper.selectAll();
		if (Objects.isNull(advisors)) throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED);
		return advisors;
	}

	@GetMapping(path = "/{id}")
	public Map<String, Object> selectById(@PathVariable String id) {
		Map<String, Object> user = in005tMapper.selectById(id);
		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return user;
	}

	@GetMapping(path = "/{id}/isjoined")
	public String isjoined(@PathVariable String id) {
		Map<String, Object> user = in005tMapper.selectById(id);
		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return String.valueOf(user.get("email"));
	}

	@PostMapping(path = "/auth")
	public Map<String, Object> auth(
		@RequestParam(name = "id", required = true) String id,
		@RequestParam(name = "pwd", required = true) String plainPassword
	) {
		Map<String, Object> user = in005tMapper.selectById(id);

		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		if (!InsuStringUtil.Equals(String.valueOf(user.get("use_yn")), "Y")) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "탈퇴한 사용자입니다.재가입하시기 바랍니다");
		}

		String pwdOnDatabase = String.valueOf(user.get("pwd"));

		try {
			if (!pwdOnDatabase.equals(UserInfoCyper.EncryptPassword(id, plainPassword))) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid pwd");
			} else {
				user.remove("pwd");
				String decMobile = UserInfoCyper.DecryptMobile(String.valueOf(user.get("mobile")));
				user.put("mobile", decMobile);
				user.put("token", InsuAuthentication.CreateToken(id));
				return user;
			}
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		}
	}

	@GetMapping(path = "/email")
	public List<Map<String, Object>> email(
		@RequestParam(name = "name", required = true) String name,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile,
		@RequestParam(name = "jumina", required = true) String jumina,
		@RequestParam(name = "sex", required = true) String sex
	) {
		try {
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			List<Map<String, Object>> list = in005tMapper.findId(name, teltype, encMobile, jumina, sex);
			if (Objects.isNull(list) || list.size() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no user");
			return list;
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화 오류.");
		}
	}

	@GetMapping(path = "/{id}/can-change")
	public String canChange(
		@PathVariable String id,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile
	) {
		Map<String, Object> user = in005tMapper.selectById(id);

		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no user");

		try {
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			if (user.get("teltype").equals(teltype) && user.get("mobile").equals(encMobile)) {
				return "OK";
			} else {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid user info");
			}
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@PutMapping(path = "/{id}/pwd")
	public String pwd(
		@PathVariable String id,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile,
		@RequestParam(name = "newPwd", required = true) String newPwd
	) {
		Map<String, Object> user = in005tMapper.selectById(id);

		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		try {
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			if (user.get("teltype").equals(teltype) && user.get("mobile").equals(encMobile)) {
				String encPwd = UserInfoCyper.EncryptPassword(id, newPwd);
				in005tMapper.updatePwd(id, encPwd);
				return "OK";
			} else {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "mismatch");
			}
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "암호화오류.");
		}
	}

	@PutMapping(path = "/{id}/juminb")
	public String juminb(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@PathVariable String id,
		@RequestParam(name = "juminb", required = true) String juminb
	) {
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, id);
			Integer.parseInt(juminb); // isValidNumber
			String encJuminb = UserInfoCyper.EncryptJuminb(juminb);

			in005tMapper.updateJuminb(id, encJuminb);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 주민번호 뒷자리입니다.");
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@PutMapping(path = "/{id}/basic")
	public String basic(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@PathVariable String id,
		@RequestParam(name = "name", required = true) String name,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile,
		@RequestParam(name = "jumina", required = true) String jumina,
		@RequestParam(name = "sex", required = true) String sex
	) {
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, id);

			Integer.parseInt(mobile); // isValidNumber
			Integer.parseInt(jumina); // isValidNumber
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			in005tMapper.updateBasic(id, name, teltype, encMobile, jumina, sex);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid number");
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화 오류.");
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@PutMapping(path = "/{id}/business")
	public String business(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@PathVariable String id,
		@RequestParam(name = "comname", required = true) String comname,
		@RequestParam(name = "sosok", required = true) String sosok,
		@RequestParam(name = "businessnum", required = true) String businessnum
	) {
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, id);

			Integer.parseInt(businessnum); // isValidNumber
			in006tMapper.merge(id, comname, sosok, businessnum);
			return "OK";
		} catch (NumberFormatException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid number");
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@PutMapping(path = "/{id}/quit")
	public String quit(@RequestHeader(name = "Authorization", required = false) String auth_header, @PathVariable String id) {
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, id);

			Map<String, Object> user = in005tMapper.selectById(id);
			if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");

			in005tMapper.updateUseYN(id, "N");
			return "OK";
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	@GetMapping(path = "/{id}/certificates")
	public List<Map<String, Object>> certificates(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@PathVariable String id
	) {
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, id);
			List<Map<String, Object>> list = in003t_v1Mapper.selectByUserId(id);
			for (Map<String, Object> certi : list) {
				String pbohumja_mobile = UserInfoCyper.DecryptMobile(String.valueOf(certi.get("pbohumja_mobile")));
				certi.put("pbohumja_mobile", pbohumja_mobile);
				String insurant_a_mobile = UserInfoCyper.DecryptMobile(String.valueOf(certi.get("insurant_a_mobile")));
				certi.put("insurant_a_mobile", insurant_a_mobile);
				String jumin = UserInfoCyper.DecryptJuminb(String.valueOf(certi.get("jumin")));
				certi.put("jumin", jumin);
				// 주택화재인 경우 적용 담보를 조회한다.
				if (InsuStringUtil.Equals(String.valueOf(certi.get("prod_code")), "m002")) {
					List<Map<String, Object>> premiums = in002t_v1Mapper.selectById(String.valueOf(certi.get("quote_no")));
					certi.put("premiums", premiums);
				}
			}
			return list;
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 주민번호 뒷자리입니다.");
		} catch (InsuEncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}
	// @GetMapping(path = "/{id}/test")
	// public List<Map<String, Object>> test(@PathVariable String id) {
	// 	List<Map<String, Object>> list = in003t_v1Mapper.selectByUserId(id);
	// 	log.debug(list.toString());
	// 	return list;
	// }
}
