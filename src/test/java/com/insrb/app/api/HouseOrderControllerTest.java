package com.insrb.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
public class HouseOrderControllerTest {

	private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";
	private static final String USER_ID = "miopadrex@gmail.com";

	@Autowired
	private MockMvc mockMvc;

	@Value("classpath:mock/p_order_card.json")
	private Resource p_order_card_json;

	@Value("classpath:mock/p_order_vacct.json")
	private Resource p_order_vacct_json;

	@Test
	@DisplayName("UI-APP-028 카드 계약등록")
	public void UIAPP028_01() throws Exception {
		String json = ResourceUtil.asString(p_order_card_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/orders/card")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(USER_ID))
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("UI-APP-028 가상계좌 계약등록")
	public void UIAPP028_02() throws Exception {
		String json = ResourceUtil.asString(p_order_vacct_json);
		mockMvc
			.perform(
				post("http://localhost:8080/house/orders/vacct")
					.header("X-insr-servicekey", SERVICE_KEY)
					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(USER_ID))
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	// @Value("classpath:mock/phone_certificate_data.json")
	// private Resource phone_certificate_data_json;

	// @Value("classpath:mock/terms.json")
	// private Resource terms_json;

	// private String QUOTE_NO = "q20210128111209.530";
	// private String USER_ID = "vingorius@gmail.com";

	// @Test
	// @DisplayName("UI-APP-027 약관동의 저장")
	// public void UIAPP027_01() throws Exception {
	// 	String json = ResourceUtil.asString(terms_json);
	// 	mockMvc
	// 		.perform(
	// 			post("http://localhost:8080/house/orders/" + QUOTE_NO + "/terms")
	// 				.header("X-insr-servicekey", SERVICE_KEY)
	// 				.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(USER_ID))
	// 				.contentType(MediaType.APPLICATION_JSON)
	// 				.content(json)
	// 		)
	// 		.andDo(print())
	// 		.andExpect(status().isOk());
	// }





	// @Test
	// @DisplayName("UI-APP-029 본인인증 결과 저장 -> OKCert로 이동")
	// public void UIAPP029_01() throws Exception {
	// 	String json = ResourceUtil.asString(phone_certificate_data_json);
	// 	mockMvc
	// 		.perform(
	// 			post("http://localhost:8080/house/orders/" + QUOTE_NO + "/phone-certification")
	// 				.header("X-insr-servicekey", SERVICE_KEY)
	// 				.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(USER_ID))
	// 				.contentType(MediaType.APPLICATION_JSON)
	// 				.content(json)
	// 		)
	// 		.andDo(print())
	// 		.andExpect(status().isOk());
	// }


// 	@Test
// 	@DisplayName("UI-APP-030 계약완료")
// 	public void UIAPP030_01() throws Exception {
// 		mockMvc
// 			.perform(
// 				get("http://localhost:8080/house/orders/" + QUOTE_NO )
// 					.header("X-insr-servicekey", SERVICE_KEY)
// 					.header(InsuAuthentication.HEADER_STRING, InsuAuthentication.GetAuthorizationValue(USER_ID))
// 					.param("user_id", USER_ID)

// 			)
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(MockMvcResultMatchers.jsonPath("$.quote_no").value(QUOTE_NO));
// 			;
// 	}	
}
