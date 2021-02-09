package com.insrb.app.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.HashMap;
import java.util.Map;
import com.insrb.app.mapper.IN005TMapper;
import com.insrb.app.util.InsuAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
public class UsersControllerTest {

	private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

	@Autowired
	IN005TMapper in005tMapper;

	@Autowired
	private MockMvc mockMvc;

	Map<String, String> mockUser;

	{
		mockUser = new HashMap<String, String>();
		mockUser.put("email", "vingorius@gmail.com");
		mockUser.put("name", "김종호");
		mockUser.put("teltype", "LG알뜰폰");
		mockUser.put("mobile", "01047011234");
		mockUser.put("pwd", "1234!!!");
		mockUser.put("jumina", "980101");
		mockUser.put("juminb", "094910");
		mockUser.put("sex", "1");

		mockUser.put("bad_teltype", "LG알뜰폰xxxx");
		mockUser.put("bad_pwd", "1234!!!xxxxx");
		mockUser.put("newPwd", "1234!!!");
	}

	Map<String, String> mockGAUser;

	{
		mockGAUser = new HashMap<String, String>();
		mockGAUser.put("email", "vingorius2@gmail.com");
		mockGAUser.put("name", "GA김종호");
		mockGAUser.put("teltype", "LG알뜰폰");
		mockGAUser.put("mobile", "01047011234");
		mockGAUser.put("pwd", "1234!!!");
		mockGAUser.put("jumina", "980101");
		mockGAUser.put("juminb", "094910");
		mockGAUser.put("sex", "1");
		mockGAUser.put("comname", "아이넷서비스코리아2");
		mockGAUser.put("sosok", "");
		mockGAUser.put("businessnum", "1918600071");
	}

	private String badUser = "_notexistuser";

	@Test
	@DisplayName("테스트 전: 이미 DB에 존재하는 사용자는 삭제한다")
	public void UIAPP000_01() throws Exception {
		in005tMapper.delete(mockUser.get("email"));
		in005tMapper.delete(mockGAUser.get("email"));
	}

	@Test
	@DisplayName("UI-APP-004 회원가입:  POST /users 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP004_01() throws Exception {
		mockMvc.perform(post("/users").header("X-insr-servicekey", SERVICE_KEY)).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-004 회원가입: POST /users 전화번호가 숫자가 아니면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP004_02() throws Exception {
		mockMvc
			.perform(
				post("/users")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("email", mockUser.get("email"))
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile") + "xxxx")
					.param("pwd", mockUser.get("pwd"))
					.param("jumina", mockUser.get("jumina"))
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-004 회원가입: POST /users 주민번호가 숫자가 아니면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP004_03() throws Exception {
		mockMvc
			.perform(
				post("/users")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("email", mockUser.get("email"))
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("pwd", mockUser.get("pwd"))
					.param("jumina", mockUser.get("jumina") + "xxxx")
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-004 회원가입: POST /users 일반사용자를 등록하고 200 OK를 리턴해야한다")
	public void UIAPP004_04() throws Exception {
		mockMvc
			.perform(
				post("/users")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("email", mockUser.get("email"))
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("pwd", mockUser.get("pwd"))
					.param("jumina", mockUser.get("jumina"))
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-004 개인사업자 회원가입: POST /users/business 개인사업자를 등록하고 200 OK를 리턴해야한다")
	public void UIAPP004_05() throws Exception {
		mockMvc
			.perform(
				post("/users/business")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("email", mockGAUser.get("email"))
					.param("name", mockGAUser.get("name"))
					.param("teltype", mockGAUser.get("teltype"))
					.param("mobile", mockGAUser.get("mobile"))
					.param("pwd", mockGAUser.get("pwd"))
					.param("jumina", mockGAUser.get("jumina"))
					.param("sex", mockGAUser.get("sex"))
					.param("comname", mockGAUser.get("comname"))
					.param("sosok", mockGAUser.get("sosok"))
					.param("businessnum", mockGAUser.get("businessnum"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-005 가입여부 확인(이메일 중복):  GET /users/{user_id}/isjoined 존재하는 사용자는 이름과 200 OK를 리턴해야한다")
	public void UIAPP005_01() throws Exception {
		mockMvc
			.perform(get("/users/" + mockUser.get("email") + "/isjoined").header("X-insr-servicekey", SERVICE_KEY))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(mockUser.get("email"))));
	}

	@Test
	@DisplayName("UI-APP-005 가입여부 확인(이메일 중복): GET /users/{user_id}/isjoined 존재하지 않는 사용자는 404 NOT_FOUND를 리턴해야한다")
	public void UIAPP005_02() throws Exception {
		mockMvc
			.perform(get("/users/" + badUser + "/isjoined").header("X-insr-servicekey", SERVICE_KEY))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("UI-APP-005 주민번호뒷자리 Update: PUT /users/{user_id}/juminb 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP005_03() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/juminb")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName(
		"UI-APP-005 주민번호뒷자리 Update: PUT /users/{user_id}/juminb Header에 bearer 토큰이 없으면 401 UNAUTHORIZED 오류를 발생한다"
	)
	public void UIAPP005_04() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/juminb")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("juminb", mockUser.get("juminb"))
			)
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("UI-APP-005 주민번호뒷자리 Update: PUT /users/{user_id}/juminb 숫자가 아니면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP005_05() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/juminb")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
					.param("pwd", mockUser.get("pwd"))
					.param("juminb", mockUser.get("juminb") + "AAA")
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-005 주민번호뒷자리 Update: PUT /users/{user_id}/juminb 올바른 정보를 제공하면 juminb를 변경하고 OK를 리턴해야한다")
	public void UIAPP005_06() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/juminb")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
					.param("juminb", mockUser.get("juminb"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-008 로그인:  POST /users/auth 파라미터 id, pwd가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP008_01() throws Exception {
		mockMvc.perform(post("/users/auth").header("X-insr-servicekey", SERVICE_KEY)).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-008 로그인: POST /users/auth 패스워드가 틀리면 401 UNAUTHORIZED를 리턴해야한다")
	public void UIAPP008_02() throws Exception {
		mockMvc
			.perform(
				post("/users/auth")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("id", mockUser.get("email"))
					.param("pwd", "invalidPassword")
			)
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("UI-APP-008 로그인: POST /users/{user_id}/login 로그인하면 토큰과 사용자정보 200 OK를 리턴해야한다")
	public void UIAPP008_03() throws Exception {
		mockMvc
			.perform(
				post("/users/auth")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("id", mockUser.get("email"))
					.param("pwd", mockUser.get("pwd"))
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("$.email").value(mockUser.get("email")));
	}

	@Test
	@DisplayName("UI-APP-009 이메일 찾기(조회): GET /users/email 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP009_01() throws Exception {
		mockMvc.perform(get("/users/email").header("X-insr-servicekey", SERVICE_KEY)).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-009 이메일 찾기(조회): GET /users/email 파라미터값이 하나라도 틀리면 404 NOT_FOUND를 리턴해야한다")
	public void UIAPP009_02() throws Exception {
		mockMvc
			.perform(
				get("/users/email")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("bad_teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("jumina", mockUser.get("jumina"))
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("UI-APP-009 이메일 찾기(조회): GET /users/email 사용자정보를 제공하면 email과 200 OK를 리턴해야한다")
	public void UIAPP009_03() throws Exception {
		mockMvc
			.perform(
				get("/users/email")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("jumina", mockUser.get("jumina"))
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(mockUser.get("email"))));
	}

	@Test
	@DisplayName("UI-APP-011 비밀번호 찾기(조회): GET /users/{user_id}/can-change 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP011_01() throws Exception {
		mockMvc
			.perform(get("/users/" + mockUser.get("email") + "/can-change").header("X-insr-servicekey", SERVICE_KEY))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-011 비밀번호 찾기(조회):GET /users/{user_id}/can-change 파라미터값이 하나라도 틀리면 404 NOT_FOUND를 리턴해야한다")
	public void UIAPP011_02() throws Exception {
		mockMvc
			.perform(
				get("/users/" + mockUser.get("email") + "/can-change")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("teltype", mockUser.get("bad_teltype"))
					.param("mobile", mockUser.get("mobile"))
			)
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("UI-APP-011 비밀번호 찾기(조회):GET /users/{user_id}/can-change 사용자정보를 제공하면 200 OK를 리턴해야한다")
	public void UIAPP011_03() throws Exception {
		mockMvc
			.perform(
				get("/users/" + mockUser.get("email") + "/can-change")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-012 비밀번호 찾기(비밀번호 변경): PUT /users/{user_id}/pwd 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP012_01() throws Exception {
		mockMvc
			.perform(put("/users/" + mockUser.get("email") + "/pwd").header("X-insr-servicekey", SERVICE_KEY))
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName(
		"UI-APP-012 비밀번호 찾기(비밀번호 변경): PUT /users/{user_id}/pwd 파라미터값이 하나라도 틀리면 404 NOT_FOUND를 리턴해야한다"
	)
	public void UIAPP012_02() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/pwd")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("teltype", mockUser.get("bad_teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("newPwd", mockUser.get("newPwd"))
			)
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName(
		"UI-APP-012 비밀번호 찾기(비밀번호 변경): PUT /users/{user_id}/pwd 올바른 정보를 제공하면 패스워드를 변경하고 OK를 리턴해야한다"
	)
	public void UIAPP012_03() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/pwd")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("newPwd", mockUser.get("newPwd"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName(
		"UI-APP-014 회원정보변경(기본정보): PUT /users/{user_id}/basic Header에 bearer 토큰이 없으면 401 UNAUTHORIZED 오류를 발생한다"
	)
	public void UIAPP014_01() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/basic")
					.header("X-insr-servicekey", SERVICE_KEY)
					// .header(Authentication.HEADER_STRING,Authentication.GetAuthorizationValue(mockUser.get("email")))
					.param("pwd", mockUser.get("bad_pwd"))
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("jumina", mockUser.get("jumina"))
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("UI-APP-014 회원정보변경(기본정보): PUT /users/{user_id}/basic 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP014_02() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/basic")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-014 회원정보변경(기본정보): PUT /users/{user_id}/basic 숫자가 아니면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP014_03() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/basic")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("jumina", mockUser.get("jumina") + "xxx")
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName(
		"UI-APP-014 회원정보변경(기본정보): PUT /users/{user_id}/basic 올바른 정보를 제공하면 basic정보를 변경하고 OK를 리턴해야한다"
	)
	public void UIAPP014_04() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/basic")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
					.param("name", mockUser.get("name"))
					.param("teltype", mockUser.get("teltype"))
					.param("mobile", mockUser.get("mobile"))
					.param("jumina", mockUser.get("jumina"))
					.param("sex", mockUser.get("sex"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName(
		"UI-APP-016 회원정보변경(사업자정보): PUT /users/{user_id}/business Header에 bearer 토큰이 없으면 401 UNAUTHORIZED 오류를 발생한다"
	)
	public void UIAPP016_01() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockGAUser.get("email") + "/business")
					.header("X-insr-servicekey", SERVICE_KEY)
					// .header(Authentication.HEADER_STRING,Authentication.GetAuthorizationValue(mockUser.get("email")))
					.param("comname", mockGAUser.get("comname"))
					.param("sosok", mockGAUser.get("sosok"))
					.param("businessnum", mockGAUser.get("businessnum"))
			)
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("UI-APP-016 회원정보변경(사업자정보): PUT /users/{user_id}/business 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP016_02() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockGAUser.get("email") + "/business")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockGAUser.get("email")))
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("UI-APP-016 회원정보변경(사업자정보): PUT /users/{user_id}/business 숫자가 아니면 400 BAD_REQUEST를 리턴해야한다")
	public void UIAPP016_03() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockGAUser.get("email") + "/business")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockGAUser.get("email")))
					.param("comname", mockGAUser.get("comname"))
					.param("sosok", mockGAUser.get("sosok"))
					.param("businessnum", mockGAUser.get("businessnum") + "xxx")
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName(
		"UI-APP-016 회원정보변경(사업자정보): PUT /users/{user_id}/business 올바른 정보를 제공하면 ga_detail정보를 변경하고 OK를 리턴해야한다"
	)
	public void UIAPP016_04() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockGAUser.get("email") + "/business")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockGAUser.get("email")))
					.param("comname", mockGAUser.get("comname"))
					.param("sosok", mockGAUser.get("sosok"))
					.param("businessnum", mockGAUser.get("businessnum"))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-045 회원탈퇴 : PUT /users/{user_id}/quit in005t.use_yn 을 'N' 처리하고 OK를 리턴해야한다")
	public void UIAPP045_01() throws Exception {
		mockMvc
			.perform(
				put("/users/" + mockUser.get("email") + "/quit")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
			)
			.andDo(print())
			.andExpect(status().isOk());
	}


	@Test
	@DisplayName("UI-APP-050 추천인 선택 : GET /users/advisors 추천인 3건 리턴해야한다")
	public void UIAPP050_01() throws Exception {
		mockMvc
			.perform(
				get("/users/advisors")
					.header("X-insr-servicekey", SERVICE_KEY)
					// .header(Authentication.HEADER_STRING, Authentication.GetAuthorizationValue(mockUser.get("email")))
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3))
			;
	}


}
