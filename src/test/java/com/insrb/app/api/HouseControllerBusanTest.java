package com.insrb.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
public class HouseControllerBusanTest {

	private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

	@Autowired
	private MockMvc mockMvc;

	@Value("classpath:static/mock/file_for_test/1.json")
	private Resource test_1_json;

	@Value("classpath:static/mock/file_for_test/6.json")
	private Resource test_6_json;

	@Value("classpath:static/mock/file_for_test/7.json")
	private Resource test_7_json;

	@Value("classpath:static/mock/file_for_test/8.json")
	private Resource test_8_json;

	@Test
	@DisplayName("TEST-01 세대별가입: 부산광역시 해운대구 윗반송로51번길 95-29")
	public void UIAPP020_01() throws Exception {
		String json = ResourceUtil.asString(test_1_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/sedae")
					.header("X-insr-servicekey", SERVICE_KEY)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(41200000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_premium").value(0)); //단체보험미가입건으로 확인
	}


	@Test
	@DisplayName("TEST-06 세대별가입: 부산광역시 해운대구 우동 1388-4 대우마리나1차아파트  101동 202호")
	public void UIAPP020_06() throws Exception {
		String json = ResourceUtil.asString(test_6_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/sedae")
					.header("X-insr-servicekey", SERVICE_KEY)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(71950000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_premium").value(0)); //단체보험미가입건으로 확인
	}

	@Test
	@DisplayName("TEST-07 세대별가입: 부산광역시 해운대구 달맞이길117번가길 98 아남하이츠3차 에이동 202호")
	public void UIAPP020_07() throws Exception {
		String json = ResourceUtil.asString(test_7_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/sedae")
					.header("X-insr-servicekey", SERVICE_KEY)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(142150000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_premium").value(0));
	}

	@Test
	@DisplayName("TEST-08 세대별가입: 부산광역시 해운대구 재반로270번길 37-34 정우빌라 201호")
	public void UIAPP020_08() throws Exception {
		String json = ResourceUtil.asString(test_8_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/sedae")
					.header("X-insr-servicekey", SERVICE_KEY)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(31590000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_premium").value(0));
	}
}
