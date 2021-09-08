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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class BRSControllerTest {

    private static final String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

    @Autowired
    private MockMvc mockMvc;

    Map<String, String> mockAddress;
    Map<String, String> mockGeneral;

    {
        mockAddress = new HashMap<String, String>();
        //mockAddress.put("search_text", "해운대 동신아파트");
        //mockAddress.put("search_text", "전포대로 133");
        //mockAddress.put("search_text", "운화리 53-2");
        mockAddress.put("search_text", "아남하이츠3차");
//        mockAddress.put("search_text", "경기도 남양주시 조안면 다산로 698-26");
        //mockAddress.put("search_text", "달음정");
        //mockAddress.put("search_text", "부산광역시 수영구 망미2동 200-3");

    }

    @Test
    @DisplayName("StructureInformation")
    public void sit() throws Exception {
        mockMvc
                .perform(get("/brs/sturctureInformation").header("X-insr-servicekey", SERVICE_KEY).param("search", mockAddress.get("search_text")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.results.common.errorCode").value("0"));
    }

    @Test
    @DisplayName("BRS :  POST /users 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
    public void juso() throws Exception {
        mockMvc.perform(get("/brs/juso").header("X-insr-servicekey", SERVICE_KEY)).andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("BRS :  POST /users 파라미터가 없으면 400 BAD_REQUEST를 리턴해야한다")
    public void juso2() throws Exception {
        mockMvc
                .perform(get("/brs/juso").header("X-insr-servicekey", SERVICE_KEY).param("search", mockAddress.get("search_text")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.results.common.errorCode").value("0"));
    }
//admcd, InbrMnnm, InbrSlno ex) 운화리:31710,25629,0053,0002  아남하이츠3차:26350,10600,1501,0004  파르나스타워:11680,10500,0159,0008
//파르나스타워:11680,10500,0159,0008
    @Test
    @DisplayName("Test : 기본개요API")
    void get_base_code(String sigungucd, String bjdongcd, String bun, String ji) throws Exception {
        mockMvc.
                perform(
                post("http://localhost:8080/brs/basecode")
                        .header("X-insr-servicekey", SERVICE_KEY)
                        .param("sigungucd", sigungucd)
                        .param("bjdongcd", bjdongcd)
                        .param("bun",bun)
                        .param("ji",ji)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test : 총괄표제부API")
    void get_br_recap() throws Exception{
        mockMvc.
                perform(
                        post("http://localhost:8080/brs/recap")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "26290")
                                .param("bjdongcd", "10900")
                                .param("bun","1227")
                                .param("ji", "0002")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test : 표제부API")
    void get_br_title() throws Exception{
        mockMvc.
                perform(
                        post("http://localhost:8080/brs/title")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "26350")
                                .param("bjdongcd", "10800")
                                .param("bun","0442")
                                .param("ji", "0001")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    //운화리:31710,25629,0053,0002
    @Test
    @DisplayName("Test : 층별개요API")
    void get_br_flr() throws Exception{
        mockMvc.
                perform(
                        post("http://localhost:8080/brs/flr")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "26350")
                                .param("bjdongcd", "10800")
                                .param("bun","0442")
                                .param("ji", "0001")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
//아남하이츠3차:26350,10600,1501,0004
    @Test
    @DisplayName("Test : 전유공용면적API")
    void get_br_pubuse() throws Exception{
        mockMvc.
                perform(
                        post("http://localhost:8080/brs/pubuse")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "26710")
                                .param("bjdongcd", "25628")
                                .param("bun","1403")
                                .param("ji", "0000")
                                .param("dongnm", "1612")
                                .param("honm", "")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test : 전유부API")
    void get_br_expos() throws Exception{
        mockMvc.
                perform(
                        post("http://localhost:8080/brs/expos")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "31710")
                                .param("bjdongcd", "25629")
                                .param("bun","0053")
                                .param("ji", "0002")
                                .param("dongnm", "")
                                .param("honm", "")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

//  mockMvc.perform(get("/brs/juso").header("X-insr-servicekey", SERVICE_KEY)).andDo(print()).andExpect(status().isBadRequest());
//}
    //파르나스타워:11680,10500,0159,0008
//아남하이츠3차:26350,10600,1501,0004 삼익비치아파트:26500,10500,0148,0004
//운화리:31710,25629,0053,0002
    @Test
    @DisplayName("Test : navigation/group")
    void group() throws Exception {
        mockMvc.
                perform(
                        post("http://localhost:8080/brs/group")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .param("sigungucd", "26710")
                                .param("bjdongcd", "25628")
                                .param("bun","1403")
                                .param("ji", "0000")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    void service_list() throws Exception {
        mockMvc.
                perform(
                        get("http://localhost:8080/insugsc/service_list")
                                .header("X-insr-servicekey", SERVICE_KEY)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }
    //TEST : 아남하이츠3차:26350,10600,1501,0004
    @Test
    @DisplayName("APIS")
    void quoteAssessment() throws Exception{
        mockMvc.
                perform(
                        post("http://localhost:8080/apis/quotes/assessment/group")
                                .header("X-insr-servicekey", SERVICE_KEY)
                                .header("x-insurobo-authorization", "32132132")
                                .header("apiVersion", "1")
                                .param("sigungucd", "26350")
                                .param("bjdongcd", "10600")
                                .param("bun","1501")
                                .param("ji", "0004")
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

}
