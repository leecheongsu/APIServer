package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.tomcat.util.json.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import kong.unirest.json.JSONObject;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ResourceUtilTest {
	@Value("classpath:basic/temp_preminum_req_body.json")
	private Resource tempPreminumReqBodyJson;

    @Test
    @DisplayName("Resource를 JSONObject로 변환하여야 한다.")
    public void test03_asJSONObject() throws ParseException{
        JSONObject obj = ResourceUtil.asJSONObject(tempPreminumReqBodyJson);
        assertEquals("", obj.getString("resultCode"));
    }
}
    