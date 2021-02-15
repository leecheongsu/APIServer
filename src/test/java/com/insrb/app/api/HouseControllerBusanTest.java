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

	// 단체가입으로 변경
	// @Value("classpath:mock/file_for_test/1.json")
	// private Resource test_1_json;

	@Value("classpath:mock/file_for_test/6.json")
	private Resource test_6_json;

	@Value("classpath:mock/file_for_test/7.json")
	private Resource test_7_json;

	@Value("classpath:mock/file_for_test/8.json")
	private Resource test_8_json;

	@Test
	@DisplayName("TEST-01 단체가입: 부산광역시 해운대구 윗반송로51번길 95-29(단독주택)")
	public void UIAPP020_01() throws Exception {
		// String json = ResourceUtil.asString(test_1_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/danche")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("sigungucd", "26350")
					.param("bjdongcd", "10100")
					.param("bun","0040")
					.param("ji", "0652")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(41200000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.premiums[?(@.item_id == 'BFRE')].premium").value(10670)); 
	}

	@Test
	@DisplayName("TEST-02 단체가입: 부산광역시 해운대구 달맞이길117번다길 130-1(다가구)")
	public void UIAPP020_02() throws Exception {
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/danche")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("sigungucd", "26350")
					.param("bjdongcd", "10600")
					.param("bun","1493")
					.param("ji", "0005")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(150840000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.premiums[?(@.item_id == 'BFRE')].premium").value(39067)); 
	}


	@Test
	@DisplayName("TEST-03 단체가입: 부산광역시 해운대구 우동 1388-4 대우마리나1차아파트(아파트)")
	public void UIAPP020_03() throws Exception {
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/danche")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("sigungucd", "26350")
					.param("bjdongcd", "10500")
					.param("bun","1388")
					.param("ji", "0004")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(429028980000L))
			.andExpect(MockMvcResultMatchers.jsonPath("$.premiums[?(@.item_id == 'BFRE')].premium").value(118411998)); 
	}

	
	@Test
	@DisplayName("TEST-04 단체가입: 부산광역시 해운대구 달맞이길117번가길 98 아남하이츠3차(단독이라고 했지만 아파트)")
	public void UIAPP020_04() throws Exception {
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/danche")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("sigungucd", "26350")
					.param("bjdongcd", "10600")
					.param("bun","1501")
					.param("ji", "0004")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(7009220000L))
			.andExpect(MockMvcResultMatchers.jsonPath("$.premiums[?(@.item_id == 'BFRE')].premium").value(1934544)); 
	}

	
	@Test
	@DisplayName("TEST-05 단체가입: 부산광역시 해운대구 재반로270번길 37-34 정우빌라(다세대라고 했지만 단독)")
	public void UIAPP020_05() throws Exception {
		mockMvc
			.perform(
				post("http://localhost:8080/house/quotes/danche")
					.header("X-insr-servicekey", SERVICE_KEY)
					.param("sigungucd", "26350")
					.param("bjdongcd", "10300")
					.param("bun","1584")
					.param("ji", "0008")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(222920000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.premiums[?(@.item_id == 'BFRE')].premium").value(57736)); 
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
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(29320000))
			.andExpect(MockMvcResultMatchers.jsonPath("$.amt_premium").value(0));
	}
}
