package com.insrb.app.api;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
public class OKCertControllerTest {
	private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

	@Autowired
	private MockMvc mockMvc;
	@Test
	@DisplayName("UI-APP-033-01 okcert")
	public void UIAPP033_01() throws Exception {
		mockMvc
			.perform(
				get("http://localhost:8080/okcert").header("X-insr-servicekey", SERVICE_KEY)
			)
			.andDo(print())
			.andExpect(status().isOk())
		;
	}
}
    