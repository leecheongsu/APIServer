package com.insrb.app.insurance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kong.unirest.json.JSONObject;

@SpringBootTest
public class AddressSearchTest {
        private static final String SEARCH_TXT = "해들목휴먼빌";
        @Autowired
        private AddressSearch address;

        @Test
        @DisplayName("주소 검색시 오류코드가 0 으로 오류가 나지 않아야한다.")
        public void test_getJusoList1() {
                // assertEquals("0", address.getJusoList(SEARCH_TXT).getJSONObject("results")
                // .getJSONObject("common").get("errorCode"));
        }

        @Test
        @DisplayName("주소 검색시 결과가 1이상이어야 한다.")
        public void test_getJusoList2() {
                assertEquals(true, address.getJusoList(SEARCH_TXT).getJSONObject("results")
                                .getJSONArray("juso").length() >= 1);
        }


        @Test
        @DisplayName("검색된 주소 결과로 표제부 데이터를 가져온다.")
        public void test_getHouseInfo1() {
                JSONObject bld = address.getJusoList(SEARCH_TXT).getJSONObject("results")
                                .getJSONArray("juso").getJSONObject(1);
                String sigunguCd = bld.getString("admCd").substring(0, 5);
                String bjdongCd = bld.getString("admCd").substring(5);
                int bun = Integer.parseInt(bld.getString("lnbrMnnm"));
                int ji = Integer.parseInt(bld.getString("lnbrSlno"));

                assertEquals("00",
                                address.getHouseInfo(sigunguCd, bjdongCd, bun, ji)
                                                .getJSONObject("response").getJSONObject("header")
                                                .getString("resultCode"));
        }

        @Test
        @DisplayName("검색된 주소 결과  이름이 검색시 사용한 빌딩이름과 같아야 한다.")
        public void test_getHouseInfo2() {
                JSONObject bld = address.getJusoList(SEARCH_TXT).getJSONObject("results")
                                .getJSONArray("juso").getJSONObject(1);
                String sigunguCd = bld.getString("admCd").substring(0, 5);
                String bjdongCd = bld.getString("admCd").substring(5);
                int bun = Integer.parseInt(bld.getString("lnbrMnnm"));
                int ji = Integer.parseInt(bld.getString("lnbrSlno"));
                // assertEquals("a", bld.toString());
                // assertEquals("11380", sigunguCd);
                // assertEquals("10800", bjdongCd);
                // assertEquals("0054", bun);
                // assertEquals("0025", ji);
                assertEquals(SEARCH_TXT,
                                address.getHouseInfo(sigunguCd, bjdongCd, bun, ji)
                                                .getJSONObject("response").getJSONObject("body")
                                                .getJSONObject("items").getJSONArray("item")
                                                .getJSONObject(1).getString("bldNm")); // .getJSONObject("item")
        }

}
