package com.insrb.app.api;

import com.insrb.app.exception.AuthException;
import com.insrb.app.exception.EncryptException;
import com.insrb.app.mapper.GaDetailsMapper;
import com.insrb.app.mapper.IN005CMapper;
import com.insrb.app.mapper.UserinfoMapper;
import com.insrb.app.util.Authentication;
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
	UserinfoMapper userinfoMapper;

	@Autowired
	GaDetailsMapper gaDetailMapper;

	@Autowired
	IN005CMapper in005cMapper;

	@GetMapping(path = "today")
	public String today() {
		log.info("Today is called");
		return userinfoMapper.getCurrentDateTime();
	}

	// Example For procedure's cursor
	// @GetMapping(path = "/all")
	// public List<Map<String, Object>> selectAll() {
	// Map<String, Object> param = new Map<String, Object>();
	// userinfoMapper.selectAll(param);
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
			int result = userinfoMapper.insert(email, name, teltype, encMobile, encPwd, jumina, sex, utype);
			if (result < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid juminb");
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
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
			int result = userinfoMapper.insert(email, name, teltype, encMobile, encPwd, jumina, sex, utype);
			if (result < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
			gaDetailMapper.merge(email, comname, sosok, businessnum);
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid juminb");
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
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
		if (Objects.isNull(advisors)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return advisors;
	}

	@GetMapping(path = "/{id}")
	public Map<String, Object> selectById(@PathVariable String id) {
		Map<String, Object> user = userinfoMapper.selectById(id);
		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return user;
	}

	@GetMapping(path = "/{id}/isjoined")
	public String isjoined(@PathVariable String id) {
		Map<String, Object> user = userinfoMapper.selectById(id);
		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		return (String) user.get("email");
	}

	@PostMapping(path = "/auth")
	public Map<String, Object> auth(
		@RequestParam(name = "id", required = true) String id,
		@RequestParam(name = "pwd", required = true) String plainPassword
	) {
		Map<String, Object> user = userinfoMapper.selectById(id);

		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		if (!InsuStringUtil.equals((String) user.get("use_yn"), "Y")) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "탈퇴한 사용자입니다.재가입하시기 바랍니다");
		}

		String pwdOnDatabase = (String) user.get("pwd");

		try {
			if (!pwdOnDatabase.equals(UserInfoCyper.EncryptPassword(id, plainPassword))) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid pwd");
			} else {
				user.remove("pwd");
				String decMobile = UserInfoCyper.DecryptMobile((String) user.get("mobile"));
				user.put("mobile", decMobile);
				user.put("token", Authentication.GetToken(id));
				return user;
			}
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		}
	}

	@GetMapping(path = "/email")
	public String email(
		@RequestParam(name = "name", required = true) String name,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile,
		@RequestParam(name = "jumina", required = true) String jumina,
		@RequestParam(name = "sex", required = true) String sex
	) {
		try {
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			String email = userinfoMapper.findId(name, teltype, encMobile, jumina, sex);
			if (Objects.isNull(email)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no user");
			return email;
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@GetMapping(path = "/{id}/can-change")
	public String canChange(
		@PathVariable String id,
		@RequestParam(name = "teltype", required = true) String teltype,
		@RequestParam(name = "mobile", required = true) String mobile
	) {
		Map<String, Object> user = userinfoMapper.selectById(id);

		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no user");

		try {
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			if (
				user.get("teltype").equals(teltype) && user.get("mobile").equals(encMobile)
			) return "OK"; else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid user info");
		} catch (EncryptException e) {
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
		Map<String, Object> user = userinfoMapper.selectById(id);

		if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		try {
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			if (user.get("teltype").equals(teltype) && user.get("mobile").equals(encMobile)) {
				String encPwd = UserInfoCyper.EncryptPassword(id, newPwd);
				userinfoMapper.updatePwd(id, encPwd);
				return "OK";
			} else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "mismatch");
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@PutMapping(path = "/{id}/juminb")
	public String juminb(
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@PathVariable String id,
		@RequestParam(name = "juminb", required = true) String juminb
	) {
		try {
			Authentication.ValidateAuthHeader(auth_header, id);
			Integer.parseInt(juminb); // isValidNumber
			String encJuminb = UserInfoCyper.EncryptJuminb(juminb);

			userinfoMapper.updateJuminb(id, encJuminb);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid juminb");
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
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
			Authentication.ValidateAuthHeader(auth_header, id);

			Integer.parseInt(mobile); // isValidNumber
			Integer.parseInt(jumina); // isValidNumber
			String encMobile = UserInfoCyper.EncryptMobile(mobile);
			userinfoMapper.updateBasic(id, name, teltype, encMobile, jumina, sex);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid number");
		} catch (EncryptException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
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
			Authentication.ValidateAuthHeader(auth_header, id);

			Integer.parseInt(businessnum); // isValidNumber
			gaDetailMapper.merge(id, comname, sosok, businessnum);
			return "OK";
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid number");
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		}
	}

	@PutMapping(path = "/{id}/quit")
	public String quit(@RequestHeader(name = "Authorization", required = false) String auth_header, @PathVariable String id) {
		try {
			Authentication.ValidateAuthHeader(auth_header, id);

			Map<String, Object> user = userinfoMapper.selectById(id);
			if (Objects.isNull(user)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

			userinfoMapper.updateUseYN(id, "N");
			return "OK";
		} catch (AuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		}
	}
}
