package com.insrb.app.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class HsartControllerTest {

    private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Test : hasartbyLee")
    public void can() throws Exception {
        mockMvc
                .perform(
                        post("http://localhost:8080/hsart/can")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "31710")
                                .param("bjdongcd", "25629")
                                .param("bun","0053")
                                .param("ji", "0002")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amt_ins").value(7009220000L));
    }

    @Test
    @DisplayName("Test : 암호화")
    void lotte_e() throws Exception {
        mockMvc
                .perform(
                        post("http://localhost:8080/hsart/lotteE")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("ctmnm", "이청수")
                                .param("ctmBirth", "930723")
                                .param("ctmTlsno","5172")
                                .param("ppaBirth", "610723")
                                .param("ppaDscno", "1")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test : 복호화")
    void lotte_d() throws Exception {
        mockMvc
                .perform(
                        post("http://localhost:8080/hsart/lotteD")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "31710")
                                .param("bjdongcd", "25629")
                                .param("bun","0053")
                                .param("ji", "0002")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
}
