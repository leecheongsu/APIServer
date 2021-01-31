package com.insrb.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insrb.app.util.ResourceUtil;
import java.util.HashMap;
import java.util.Map;
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

	private static final String ACCESS_KEY = "myValue";

	@Autowired
	private MockMvc mockMvc;

	@Value("classpath:static/mock/file_for_test/address.json")
	private Resource address_json;

	Map<String, String> mockAddress;

	{
		mockAddress = new HashMap<String, String>();
		mockAddress.put("search_text", "해들목휴먼빌");
	}

	@Test
	@DisplayName("UI-APP-033-01 풍수해 주소찾기")
	public void UIAPP033_01() throws Exception {
		mockMvc
			.perform(
				get("http://localhost:8080/ww/juso").header("X-insr-servicekey", ACCESS_KEY).param("search", mockAddress.get("search_text"))
			)
			.andDo(print())
			.andExpect(status().isOk())// .andExpect(MockMvcResultMatchers.jsonPath("$.code.length()").value(51))
		// .andExpect(MockMvcResultMatchers.jsonPath("$.template.oagi6002vo.ptyKorNm").value("이청수"));
		;
	}

	@Test
	@DisplayName("UI-APP-033-01 풍수해 주소찾기, Cover 요청")
	public void UIAPP033_02() throws Exception {
		mockMvc
			.perform(
				get("http://localhost:8080/ww/cover")
					.header("X-insr-servicekey", ACCESS_KEY)
					.param("sigungucd", "11410")
					.param("bjdongcd","11900")
					.param("bun", "0002")
					.param("ji", "0002")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.code.length()").value(51))
			.andExpect(MockMvcResultMatchers.jsonPath("$.template.oagi6002vo.ptyKorNm").value("이청수"));
	}

	@Test
	@DisplayName("UI-APP-036-01 풍수해 가보험료 확인")
	public void UIAPP036_01() throws Exception {
		String json = ResourceUtil.asString(address_json);
		mockMvc
			.perform(
				post("http://localhost:8080/ww/pre-premium")
					.header("X-insr-servicekey", ACCESS_KEY)
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
}
