package com.insrb.app.api;

import com.insrb.app.exception.SearchException;
import com.insrb.app.insurance.BldRgstService;
import com.insrb.app.mapper.IN400CMapper;
import com.insrb.app.util.BrsUtil;
import com.insrb.app.util.InsuStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;


@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/brs")
public class BRSController {

    @Autowired
    BldRgstService bldRgstService;

    @Autowired
    IN400CMapper in400CMapper;

    private Object GeneralStructureInformation;


    //도로명 주소 매핑
    @GetMapping(path = "juso")
    public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
        try {
            return bldRgstService.getJusoList(search);
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        }
    }

    @PostMapping(path = "basecode")
    public List<Map<String, Object>> get_base_code(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji
    ){
        log.debug("base_code:{},{},{},{}", sigungucd, bjdongcd, bun, ji);

        try {
            Map<String, Object> search = bldRgstService.getBrBasisOulnInfo(sigungucd, bjdongcd, bun, ji);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }

    @PostMapping(path = "recap")
    public List<Map<String, Object>> get_br_recap(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji
    ){
        log.debug("br_recap:{},{},{},{}", sigungucd, bjdongcd, bun, ji);

        try {
            Map<String, Object> search = bldRgstService.getBrRecapTitleInfo(sigungucd, bjdongcd, bun, ji);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

    @PostMapping(path="title")
    public List<Map<String, Object>> get_br_title(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji
    ){
        log.debug("br_title:{},{},{},{}", sigungucd, bjdongcd, bun, ji);
        try {
            Map<String, Object> search = bldRgstService.getBrTitleInfo(sigungucd, bjdongcd, bun, ji);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

    @PostMapping(path="flr")
    public List<Map<String, Object>> get_br_flr(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji
    ){
        log.debug("br_flr:{},{},{},{}", sigungucd, bjdongcd, bun, ji);
        try {
            Map<String, Object> search = bldRgstService.getBrFlrOulnInfo(sigungucd, bjdongcd, bun, ji);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }

    @PostMapping(path="pubuse")
    public List<Map<String, Object>> get_br_pubuse(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji,
            @RequestParam(name = "dongnm", required = true) String dongnm,
            @RequestParam(name = "honm", required = false) String honm
    ){
        log.debug("br_pubuse:{},{},{},{}", sigungucd, bjdongcd, bun, ji);
        try {
            Map<String, Object> search = bldRgstService.getBrExposPubuseAreaInfo(sigungucd, bjdongcd, bun, ji, dongnm, honm);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

    @PostMapping(path = "expos")
    public List<Map<String, Object>> get_br_expos(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji,
            @RequestParam(name = "dongnm", required = true) String dongnm,
            @RequestParam(name = "honm", required = false) String honm
    ){
        log.debug("get_expos:{},{},{},{}", sigungucd, bjdongcd, bun, ji);
        try {
            Map<String, Object> search = bldRgstService.getBrExposInfo(sigungucd, bjdongcd, bun, ji, dongnm, honm);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }


    @PostMapping(path = "group")
    public List<Map<String, Object>> group(
            @RequestParam(name = "sigungucd", required = true) String sigungucd,
            @RequestParam(name = "bjdongcd", required = true) String bjdongcd,
            @RequestParam(name = "bun", required = true) int bun,
            @RequestParam(name = "ji", required = true) int ji
    ){
        log.debug("group: {}, {}, {}, {}", sigungucd, bjdongcd, bun, ji);

        try {
            Map<String, Object> search = bldRgstService.getBrBasisOulnInfo(sigungucd, bjdongcd, bun, ji);
            List<Map<String, Object>> items = BrsUtil.GetFromBrs(search);

            log.debug(items.get(0).toString());
            log.debug(items.get(1).toString());
            log.debug(items.get(2).toString());

            return items;
        } catch (SearchException e) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }


    //emptyAddress
    private String getNotEmptyAddress(Object platPlc, Object newPlatPlc) {
        String address = InsuStringUtil.IsEmpty(String.valueOf(newPlatPlc)) ? String.valueOf(platPlc) : String.valueOf(newPlatPlc);
        return address;
    }

}
