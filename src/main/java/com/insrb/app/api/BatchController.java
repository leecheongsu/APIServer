package com.insrb.app.api;

import com.insrb.app.exception.InsuEncryptException;
import com.insrb.app.mapper.IN003T_V1Mapper;
import com.insrb.app.util.KakaoMessageComponent;
import com.insrb.app.util.cyper.UserInfoCyper;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/batch")
public class BatchController {

	@Autowired
	IN003T_V1Mapper in003t_v1Mapper;

	@Autowired
	KakaoMessageComponent kakaoMessage;

	@GetMapping(path = "/expire_before_30")
	public String page() throws InsuEncryptException {
		log.debug("expire_before_30");
		List<Map<String, Object>> list = in003t_v1Mapper.selectByExpireBefore30();
		for (Map<String, Object> item : list) {
			String phone = UserInfoCyper.DecryptMobile(String.valueOf(item.get("insurant_a_mobile")));
			log.debug(phone,item.get("quote_no"));
			kakaoMessage.A003(
				String.valueOf(item.get("quote_no")),
				phone,
				String.valueOf(item.get("polholder")),
				String.valueOf(item.get("prod_name")),
				String.valueOf(item.get("exp_day")),
				String.valueOf(item.get("sec_no")),
				String.valueOf(item.get("period"))
			);
		}
		return "OK";
	}
}
