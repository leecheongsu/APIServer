package com.insrb.app.api;


import com.insrb.app.exception.SearchException;
import com.insrb.app.insurance.AddressSearch;
import com.insrb.app.mapper.IN001TMapper;
import com.insrb.app.mapper.IN002TMapper;
import com.insrb.app.mapper.IN006CMapper;
import com.insrb.app.mapper.IN010TMapper;
import com.insrb.app.util.BrsUtil;
import com.insrb.app.util.InsuStringUtil;
import com.insrb.app.util.QuoteUtil;
import com.insrb.app.util.cyper.DESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/hsart")
public class HsartController {

    @Autowired
    AddressSearch addressSearch;

    @Autowired
    IN010TMapper in010tMapper;

    @Autowired
    IN001TMapper in001tMapper;

    @Autowired
    IN006CMapper in006cMapper;

    @Autowired
    IN002TMapper in002tMapper;

    @Value("classpath:basic/tmpl_preminum_req_body.json")
    private Resource tmpl_preminum_req_body_json;

    @GetMapping(path = "juso")
    public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
        try {
            return addressSearch.getJusoList(search);
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        }
    }

    @PostMapping(path = "can")
    public Map<String, Object> can(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji
    ) {
      try{

              Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);
              List<Map<String, Object>> items = QuoteUtil.GetItemFromHouseInfo(search);

              Map<String, Object> data = new HashMap<String, Object>();
              Map<String, Object> cover = BrsUtil.GetCoverSummary(items);

              String quote_no = QuoteUtil.GetNewQuoteNo("Q");
                String building_type = "DGG";



                in010tMapper.fireinsurance_insert(
                        quote_no,
                        building_type,
                        (String) getNotEmptyAddress(cover.get("platPlc"), cover.get("newPlatPlc")),
                        "T", //(String)cover.get("group_ins"),
                        String.valueOf(cover.get("bldNm")),
                        String.valueOf(cover.get("dong_info")),
                        String.valueOf(cover.get("mainPurpsCdNm")),
                        String.valueOf(cover.get("newPlatPlc")),
                        String.valueOf(cover.get("etcPurps")),
                        String.valueOf(cover.get("useAprDay")),
                        String.valueOf(cover.get("etcRoof")),
                        String.valueOf(cover.get("dongNm")),
                        String.valueOf(cover.get("total_area")),
                        String.valueOf(cover.get("cnt_sedae")),
                        String.valueOf(cover.get("grndFlrCnt")),
                        String.valueOf(cover.get("ugrndFlrCnt")),
                        String.valueOf(cover.get("etcStrct")),
                        cover.toString(),
                        "" //단체가입은 전유부가 없다.
                );


                data = in001tMapper.selectById(quote_no);

                log.debug(String.valueOf(data.get("amt_ins")));

                List<Map<String, Object>> detail = in002tMapper.selectById(quote_no);
                data.put("premiums", detail);
                Map<String, Object> product = in006cMapper.selectByPcode("m002");

          return data;

        } catch (DataAccessException e) {
          log.error(e.getMessage());
          if (e.getRootCause() instanceof SQLException) {
              SQLException sqlEx = (SQLException) e.getRootCause();
              int sqlErrorCode = sqlEx.getErrorCode();
              if(sqlErrorCode == -20101){
                  log.error("trash/can(신축단가조회오류):{},{},{},{}", sigungucd, bjdongcd, bun, ji);
                  throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "등록된 건물에 대한 단가를 찾지못했습니다.\n관리자에게 연락해주세요.");
              }
              throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,sqlEx.getMessage());
          }
          throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
      } catch (SearchException e) {
          log.error("trash/can: {}", e.getMessage());
          throw new ResponseStatusException(HttpStatus.NO_CONTENT, "시스템 점검중입니다");
      }
    }

    private Object getNotEmptyAddress(Object platPlc, Object newPlatPlc) {
        String address = InsuStringUtil.IsEmpty(String.valueOf(newPlatPlc)) ? String.valueOf(platPlc) : String.valueOf(newPlatPlc);
        return address;
    }

    @PostMapping(path = "lotteE")
    public String lotte_e (
        @RequestParam(name = "ctmnm", required = true) String ctmnm,
        @RequestParam(name = "ctmBirth", required = true) String ctmBirth,
        @RequestParam(name = "ctmTlsno", required = true) String ctmTlsno,
        @RequestParam(name = "ppaBirth", required = true) String ppaBirth,
        @RequestParam(name = "ppaDscno", required = true) String ppaDscno
    ) {
        try {
            String all = "";

            String tmp = "";

            tmp = DESUtil.encrypt(ctmnm);
            all = tmp;

            tmp = DESUtil.encrypt(ctmBirth);
            all += "/" + tmp;

            tmp = DESUtil.encrypt(ctmTlsno);
            all += "/" + tmp;

            tmp = DESUtil.encrypt(ppaBirth);
            all += "/" + tmp;

            tmp = DESUtil.encrypt(ppaDscno);
            all += "/" + tmp;

            log.debug(all.toString());

            return all;
        }
        catch (Exception e) {
            log.error("lotte/encrypt: {}", e.getMessage());
            return "";
        }
    }

    @PostMapping(path = "lotteD")
    public String lotte_d (
            @RequestParam(name = "ctmnm", required = true) String ctmnm,
            @RequestParam(name = "ctmBirth", required = true) String ctmBirth,
            @RequestParam(name = "ctmTlsno", required = true) String ctmTlsno,
            @RequestParam(name = "ppaBirth", required = true) String ppaBirth,
            @RequestParam(name = "ppaDscno", required = true) String ppaDscno
    ) {
        try {
            String all = "";

            String tmp = "";

            tmp = DESUtil.decrypt(ctmnm);
            all = tmp;

            tmp = DESUtil.decrypt(ctmBirth);
            all += "/" + tmp;

            tmp = DESUtil.decrypt(ctmTlsno);
            all += "/" + tmp;

            tmp = DESUtil.decrypt(ppaBirth);
            all += "/" + tmp;

            tmp = DESUtil.decrypt(ppaDscno);
            all += "/" + tmp;

            log.debug(all.toString());

            return all;
        }
        catch (Exception e) {
            log.error("lotte/decrypt: {}", e.getMessage());
            return "";
        }
    }


}
