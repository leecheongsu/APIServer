package com.insrb.app.api;

import com.insrb.app.mapper.IN009TMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import kcb.module.v3.exception.OkCertException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

// Return 되는 값 다음 참고
// https://docs.bootpay.co.kr/document/payment

@Slf4j
@RestController
@RequestMapping("/bootpay")
public class BootpayController {

	@Autowired
	IN009TMapper in009tMapper;

	// 아래 URL은 Filterconfig에서 Rule Out되어야 함.
	@PostMapping(path = "/rtn/house", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String rtn(HttpServletRequest request) throws OkCertException, IOException {
		log.info("bootpay return");
		return "<html></html>";
	}
}
