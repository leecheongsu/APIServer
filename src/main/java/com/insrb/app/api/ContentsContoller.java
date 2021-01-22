package com.insrb.app.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/contents")
public class ContentsContoller {

	@Value("classpath:static/mock/content.json")
	private Resource mockContent;

	@GetMapping(path = "")
	public Resource contents() {
		log.info("contents");
		return mockContent;
	}
}
