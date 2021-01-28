package com.insrb.app.api;

import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insrb.app.insurance.hi.HiWindWaterInsurance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unchecked")
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

	@PostMapping(path = "pre-preminum")
	public  Map<String, Object>  prePremium(@RequestBody(required = true) Map<String, Object> body) {
		log.info("현대해상 가보험료 요청");
		Map<String, Object> data = (Map<String, Object>) body.get("data");
		try {
            ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Map<String, Object> result = hi.getPrePremium(json);
            log.info("Result:{}",result);
            return result;
		} catch (JsonProcessingException e) {
			log.error("/house/orders: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다.");
		}
	}
}
