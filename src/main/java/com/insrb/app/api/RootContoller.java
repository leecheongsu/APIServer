package com.insrb.app.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class RootContoller {

    @GetMapping(path = "/")
    public String page() {
        log.info("First Call");
        return "Hi InsuRobo !!!";
    }
}
