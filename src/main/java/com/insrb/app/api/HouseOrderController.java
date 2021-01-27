package com.insrb.app.api;

import java.sql.Date;
import java.util.Map;
import com.insrb.app.mapper.POrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/house/orders")
public class HouseOrderController {

	@Autowired
	POrderMapper pOrderMapper;

	@PostMapping(path = "")
	public Map<String, Object> insert(@RequestBody(required = true) Map<String, Object> body) {
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        log.info(data.toString());
        String quote_no = (String)data.get("quote_no");
        String prod_code = (String)data.get("prod_code");
        String opayment = (String)data.get("opayment");
        String polholder = (String)data.get("polholder");
        String insurant_a = (String)data.get("insurant_a");
        String insurant_b = (String)data.get("insurant_b");
        String premium = (String)data.get("premium");
        String insdate = (String)data.get("insdate");
        Date ins_from = (Date)data.get("ins_from");
        Date ins_to = (Date)data.get("ins_to");
        int ptype = (int)data.get("ptype");
        String insloc = (String)data.get("insloc");
        String mobile = (String)data.get("mobile");
        String email = (String)data.get("email");
        int poption = (int)data.get("poption");
        String pbohumja_mobile = (String)data.get("pbohumja_mobile");
        String pbohumja_teltype = (String)data.get("pbohumja_teltype");
        Date odate = (Date)data.get("odate");
        String jumin = (String)data.get("jumin");
        String user_id = (String)data.get("user_id");
        int o_by = (int)data.get("o_by");
        String owner = (String)data.get("owner");
        String pbohumja_birth = (String)data.get("pbohumja_birth");
        String advisor_no = (String)data.get("advisor_no");


        pOrderMapper.delete(quote_no);
        pOrderMapper.insert(quote_no, prod_code, opayment, polholder, insurant_a, insurant_b, premium, insdate, ins_from, ins_to, ptype, insloc, mobile, email, poption, pbohumja_mobile, pbohumja_teltype, odate, jumin, user_id, o_by, owner, pbohumja_birth, advisor_no);
		return data;
	}
}
