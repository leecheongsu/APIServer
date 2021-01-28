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
public class WWControllerTest {

	private static final String ACCESS_KEY = "myValue";

	@Autowired
	private MockMvc mockMvc;

	@Value("classpath:static/mock/file_for_test/address.json")
	private Resource address_json;


	@Test
	@DisplayName("TEST-01 세대별가입: 부산광역시 해운대구 윗반송로51번길 95-29")
	public void UIAPP020_01() throws Exception {
		String json = ResourceUtil.asString(address_json);
		mockMvc
			.perform(
				post("http://localhost:8080/ww/pre-preminum")
					.header("X-insr-servicekey", ACCESS_KEY)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.oagi6002vo.giid0100vo.tpymPrem").value(91100))
            ; 
	}


}
    