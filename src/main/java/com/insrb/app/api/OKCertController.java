package com.insrb.app.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.insrb.app.mapper.IN007TMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import kcb.module.v3.exception.OkCertException;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/okcert")
public class OKCertController {

	@Autowired
	IN007TMapper in007tMapper;

	String license = "V44820000000_IDS_01_PROD_AES_license.dat";
	String SITE_NAME = "인슈로보"; // 요청사이트명
	String SITE_URL = "insrb.com";
	String CP_CD = "V44820000000";

	// String RETURN_URL = "http://"+request.getServerName()+":8080/phone_popup/phone_popup3.jsp";// 인증 완료 후 리턴될 URL (도메인 포함 full path)
	String RQST_CAUS_CD = "00";
	String target = "PROD"; // 테스트="TEST", 운영="PROD"
	//String popupUrl = "";	// 테스트 URL
	String popupUrl = "https://safe.ok-name.co.kr/CommonSvl"; // 운영 URL

	// @Value("classpath:basic/V44820000000_IDS_01_PROD_AES_license.dat")
	// private Resource licenseResource;

	@GetMapping(path = "/house")
	public Map<String, Object> page(HttpServletRequest request, @RequestParam(name = "quote_no", required = true) String quote_no)
		throws OkCertException, IOException {
		String svcName = "IDS_HS_POPUP_START";
		JSONObject reqJson = new JSONObject();
		// TODO: http --> 나중에 https 로 바꿀것...URL을 통째로 바꿀것.
		// String RETURN_URL = "http://" + request.getServerName() + ":8080/okcert/rtn"; // 인증 완료 후 리턴될 URL (도메인 포함 full path)
		
		String RETURN_URL = "http://210.179.175.145/okcert/rtn"; // 인증 완료 후 리턴될 URL (도메인 포함 full path)
		reqJson.put("RETURN_URL", RETURN_URL);
		reqJson.put("SITE_NAME", SITE_NAME);
		reqJson.put("SITE_URL", SITE_URL);
		reqJson.put("RQST_CAUS_CD", RQST_CAUS_CD);
		reqJson.put("RETURN_MSG", quote_no);
		String reqStr = reqJson.toString();
		kcb.module.v3.OkCert okcert = new kcb.module.v3.OkCert();
		String resultStr = okcert.callOkCert(target, CP_CD, svcName, license, reqStr);

		JSONObject resJson = new JSONObject(resultStr);

		String RSLT_CD = resJson.getString("RSLT_CD");
		String RSLT_MSG = resJson.getString("RSLT_MSG");
		String MDL_TKN = "";

		boolean succ = false;

		if ("B000".equals(RSLT_CD) && resJson.has("MDL_TKN")) {
			MDL_TKN = resJson.getString("MDL_TKN");
			succ = true;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("popupUrl", popupUrl);
		data.put("CP_CD", CP_CD);
		data.put("MDL_TKN", MDL_TKN);
		data.put("RSLT_CD", RSLT_CD);
		data.put("RSLT_MSG", RSLT_MSG);
		data.put("succ", succ);

		log.info("okcert:{}",data.toString());
		return data;
	}

	@PostMapping(path = "/house/rtn", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String rtn(HttpServletRequest request) throws OkCertException {
		String MDL_TKN = request.getParameter("mdl_tkn");

		// 서비스명 (고정값)
		String svcName = "IDS_HS_POPUP_RESULT";

        // okcert3 요청 정보
		JSONObject reqJson = new JSONObject();

		reqJson.put("MDL_TKN", MDL_TKN);
		String reqStr = reqJson.toString();

        // okcert3 실행
		kcb.module.v3.OkCert okcert = new kcb.module.v3.OkCert();

		String resultStr = okcert.callOkCert(target, CP_CD, svcName, license, reqStr);

		JSONObject resJson = new JSONObject(resultStr);

		String RSLT_CD = resJson.getString("RSLT_CD");
		String RSLT_MSG = resJson.getString("RSLT_MSG");
		String TX_SEQ_NO = resJson.getString("TX_SEQ_NO");

		String RSLT_NAME = "";
		String RSLT_BIRTHDAY = "";
		String RSLT_SEX_CD = "";
		String RSLT_NTV_FRNR_CD = "";

		String DI = "";
		String CI = "";
		String CI_UPDATE = "";
		String TEL_COM_CD = "";
		String TEL_NO = "";

		String RETURN_MSG = "";
		if (resJson.has("RETURN_MSG")) RETURN_MSG = resJson.getString("RETURN_MSG");
		String html = "";

		if ("B000".equals(RSLT_CD)) {
			RSLT_NAME = resJson.getString("RSLT_NAME");
			RSLT_BIRTHDAY = resJson.getString("RSLT_BIRTHDAY");
			RSLT_SEX_CD = resJson.getString("RSLT_SEX_CD");
			RSLT_NTV_FRNR_CD = resJson.getString("RSLT_NTV_FRNR_CD");

			DI = resJson.getString("DI");
			CI = resJson.getString("CI");
			CI_UPDATE = resJson.getString("CI_UPDATE");
			TEL_COM_CD = resJson.getString("TEL_COM_CD");
			TEL_NO = resJson.getString("TEL_NO");
			in007tMapper.insert(RSLT_NAME, RSLT_BIRTHDAY, RSLT_SEX_CD, RSLT_NTV_FRNR_CD, DI, CI, CI_UPDATE, TEL_COM_CD, TEL_NO, RETURN_MSG);
			html = "<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('ok');</script></html>";
		} else {
			html =  "<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('fail');</script></html>";
		}
		log.info("html:{}",html);
		return html;
	}

	// @GetMapping(path = "/hello1")
	// @ResponseBody
	// public Map<String, Object> hello1() {
	// 	Map<String, Object> data = new HashMap<String, Object>();
	// 	data.put("hi", "hello");
	// 	return data;
	// }

	// @GetMapping(path = "/hello2", produces = MediaType.TEXT_HTML_VALUE)
	// @ResponseBody
	// public String hello2() {
	// 	return "<html><title></tiel><body>Hello</body></heml>";
	// }
}
