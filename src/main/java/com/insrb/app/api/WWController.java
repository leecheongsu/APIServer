package com.insrb.app.api;

import com.insrb.app.insurance.hi.HiWindWaterInsurance;
import com.insrb.app.util.ResourceUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ww")
public class WWController {
    @Value("classpath:static/mock/address.json")
    private Resource addressJson;

    @Autowired
    private HiWindWaterInsurance hi;

    @GetMapping(path = "")
    public String index() {
        log.info("현대해상 First Call");
        return hi.getWWToken();
    }

    @GetMapping(path = "utf8test")
    public Resource address() {
        return addressJson;
    }


    @GetMapping(path = "pre-preminum")
    public String prePremium() {
        log.info("현대해상 가보험료 요청");
        return hi.getPrePremium(ResourceUtil.asString(addressJson));
    }

}
