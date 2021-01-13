package com.insrb.app.api;

import java.util.Map;
import com.insrb.app.insurance.AddressSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/house")
public class HouseController {

    @Autowired
    AddressSearch addressSearch;

    @GetMapping(path = "juso")
    public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
        log.info(search);
        return addressSearch.getJusoList(search).toMap();
    }
}
