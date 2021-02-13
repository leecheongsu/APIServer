package com.insrb.app.insurance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import com.insrb.app.exception.SearchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SuppressWarnings("unchecked")
@SpringBootTest
public class AddressSearchTest {

    private static final String SEARCH_TXT = "해들목휴먼빌";

    @Autowired
    private AddressSearch address;

    @Test
    @DisplayName("주소 검색시 오류코드가 0 으로 오류가 나지 않아야한다.")
    public void test_getJusoList1() throws SearchException {
        Map<String, Object> search = address.getJusoList(SEARCH_TXT);
        Map<String, Object> results = (Map<String, Object>) search.get("results");
        Map<String, Object> common = (Map<String, Object>) results.get("common");
        assertEquals("0", common.get("errorCode"));
    }

    @Test
    @DisplayName("주소 검색시 결과가 1이상이어야 한다.")
    public void test_getJusoList2() throws SearchException {
        Map<String, Object> search = address.getJusoList(SEARCH_TXT);
        Map<String, Object> results = (Map<String, Object>) search.get("results");
        List<Map<String, Object>> juso = (List<Map<String, Object>>) results.get("juso");
        assertEquals(true, juso.size() >= 1);
    }

    @Test
    @DisplayName("검색된 주소 결과로 표제부 데이터를 가져온다.")
    public void test_getHouseInfo1() throws SearchException {
        Map<String, Object> search = address.getJusoList(SEARCH_TXT);
        Map<String, Object> results = (Map<String, Object>) search.get("results");
        List<Map<String, Object>> juso = (List<Map<String, Object>>) results.get("juso");
        Map<String, Object> bld = juso.get(1);

        String sigunguCd = ((String) bld.get("admCd")).substring(0, 5);
        String bjdongCd = ((String) bld.get("admCd")).substring(5);
        int bun = Integer.parseInt((String) bld.get("lnbrMnnm"));
        int ji = Integer.parseInt((String) bld.get("lnbrSlno"));
        Map<String, Object> info = address.getHouseCoverInfo(sigunguCd, bjdongCd, bun, ji);
        Map<String, Object> response = (Map<String, Object>) info.get("response");
        Map<String, Object> header = (Map<String, Object>) response.get("header");
        assertEquals("00", header.get("resultCode"));
    }

    @Test
    @DisplayName("검색된 주소 결과  이름이 검색시 사용한 빌딩이름과 같아야 한다.")
    public void test_getHouseInfo2() throws SearchException {
        Map<String, Object> search = address.getJusoList(SEARCH_TXT);
        Map<String, Object> results = (Map<String, Object>) search.get("results");
        List<Map<String, Object>> juso = (List<Map<String, Object>>) results.get("juso");
        Map<String, Object> bld = juso.get(1);

        String sigunguCd = ((String) bld.get("admCd")).substring(0, 5);
        String bjdongCd = ((String) bld.get("admCd")).substring(5);
        int bun = Integer.parseInt((String) bld.get("lnbrMnnm"));
        int ji = Integer.parseInt((String) bld.get("lnbrSlno"));

        Map<String, Object> info = address.getHouseCoverInfo(sigunguCd, bjdongCd, bun, ji);

        Map<String, Object> response = (Map<String, Object>) info.get("response");
        Map<String, Object> body = (Map<String, Object>) response.get("body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        List<Map<String, Object>> item = (List<Map<String, Object>>) items.get("item");
        Map<String, Object> data = (Map<String, Object>) item.get(1);
        assertEquals(SEARCH_TXT, data.get("bldNm"));
        // assertEquals(SEARCH_TXT,
        // address.getHouseInfo(sigunguCd, bjdongCd, bun, ji)
        // .getJSONObject("response").getJSONObject("body")
        // .getJSONObject("items").getJSONArray("item")
        // .getJSONObject(1).getString("bldNm")); // .getJSONObject("item")
    }
}
