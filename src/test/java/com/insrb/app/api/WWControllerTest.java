package com.insrb.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.HashMap;
import java.util.Map;
import com.insrb.app.util.InsuAuthentication;
import com.insrb.app.util.ResourceUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
public class WWControllerTest {

	private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

	@Autowired
	private MockMvc mockMvc;

	@Value("classpath:mock/pre_premium.json")
	private Resource pre_premium_json;

	@Value("classpath:mock/premium.json")
	private Resource premium_json;

	@Value("classpath:mock/prevent_denial.json")
	private Resource prevent_denial_json;
	
	Map<String, String> mockUser;
	{
		mockUser = new HashMap<String, String>();
		mockUser.put("email", "vingorius@gmail.com"); // premium.json 에 있는 user와 동일하여야 한다.
	}

	Map<String, String> mockSearch;

	{
		mockSearch = new HashMap<String, String>();
		mockSearch.put("search_text", "두산더랜드파크");
	}

	Map<String, String> mockAddress;
	{
		mockAddress = new HashMap<String, String>();
		mockAddress.put("sigungucd", "11500");
		mockAddress.put("bjdongcd", "10500");
		mockAddress.put("bun", "757");
		mockAddress.put("ji", "0");
		mockAddress.put("zip", "07788");
	}

	@Test
	@DisplayName("UI-APP-033-01 풍수해 주소찾기")
	public void UIAPP033_01() throws Exception {
		mockMvc
			.perform(
				get("http://localhost:8080/ww/juso").header("X-insr-servicekey", SERVICE_KEY).param("search", mockSearch.get("search_text"))
			)
			.andDo(print())
			.andExpect(status().isOk())
		;
	}

	@Test
	@DisplayName("UI-APP-033-01 풍수해 Cover 요청")
	public void UIAPP033_02() throws Exception {
		mockMvc
			.perform(
				get("http://localhost:8080/ww/cover")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("sigungucd", mockAddress.get("sigungucd"))
					.param("bjdongcd", mockAddress.get("bjdongcd"))
					.param("bun", mockAddress.get("bun"))
					.param("ji", mockAddress.get("ji"))
					.param("zip", mockAddress.get("zip"))
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.premiums.length()").value(51))
			.andExpect(MockMvcResultMatchers.jsonPath("$.lobz_cds.length()").value(31))
			.andExpect(MockMvcResultMatchers.jsonPath("$.ww_info.oagi6002vo.lsgcCd").value("I004"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.ww_info.oagi6002vo.ptyKorNm").value("이청수"));
	}

	@Test
	@DisplayName("UI-APP-036-01 풍수해 가보험료 확인")
	public void UIAPP036_01() throws Exception {
		String json = ResourceUtil.asString(pre_premium_json);
		mockMvc
			.perform(
				post("http://localhost:8080/ww/pre-premium")
					.header("X-insr-servicekey", SERVICE_KEY)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk())
			// .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(0))
			.andExpect(MockMvcResultMatchers.jsonPath("$.perPrem").value(18200)) //본인 부담 보험료
			.andExpect(MockMvcResultMatchers.jsonPath("$.govtPrem").value(51600)) //정부 부담 보험료
			.andExpect(MockMvcResultMatchers.jsonPath("$.lgovtPrem").value(21300)) // 지자체 부담 보험료
			.andExpect(MockMvcResultMatchers.jsonPath("$.tpymPrem").value(91100)); //총보험료
	}

	@Test
	@DisplayName("UI-APP-03701 풍수해 실보험료 확인")
	public void UIAPP037_01() throws Exception {
		String json = ResourceUtil.asString(premium_json);
		mockMvc
			.perform(
				post("http://localhost:8080/ww/premium")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-03701 풍수해 부인방지 확인")
	public void UIAPP037_02() throws Exception {
		String json = ResourceUtil.asString(prevent_denial_json);
		mockMvc
			.perform(
				post("http://localhost:8080/ww/prevent_denial")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(mockUser.get("email")))
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk());
	}
}
