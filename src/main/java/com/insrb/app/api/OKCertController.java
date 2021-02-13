package com.insrb.app.api;

import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuAuthExpiredException;
import com.insrb.app.mapper.IN007TMapper;
import com.insrb.app.util.InsuAuthentication;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import kcb.module.v3.OkCert;
import kcb.module.v3.exception.OkCertException;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/okcert")
public class OKCertController {

	@Autowired
	private IN007TMapper in007tMapper;

	// 파일 내용은 현 환경에서 필요없고, 파일 이름만 있으면 된다.
	private static String LICENSE_FILE_NAME = "V44820000000_IDS_01_PROD_AES_license.dat";
	private static String SITE_NAME = "인슈로보"; // 요청사이트명
	private static String SITE_URL = "insrb.com";
	private static String CP_CD = "V44820000000";

	private static String RQST_CAUS_CD = "00";
	private static String TARGET_SYSTEM = "PROD"; // 테스트="TEST", 운영="PROD"
	//String popupUrl = "";	// 테스트 URL
	private static String POPUP_URL = "https://safe.ok-name.co.kr/CommonSvl"; // 운영 URL

	@Value("classpath:basic/V44820000000_IDS_01_PROD_AES_license.dat")
	private Resource licenseResource;

	@GetMapping(path = "/house")
	public Map<String, Object> page(
		HttpServletRequest request,
		@RequestHeader(name = "Authorization", required = false) String auth_header,
		@RequestParam(name = "quote_no", required = true) String quote_no,
		@RequestParam(name = "user_id", required = true) String user_id
	)
		throws OkCertException, IOException {
		try {
			InsuAuthentication.ValidateAuthHeader(auth_header, user_id);
			// 서비스명 (고정값)
			String svcName = "IDS_HS_POPUP_START";
			// TODO: http --> 나중에 https 로 바꿀것...URL을 통째로 바꿀것.
			// String RETURN_URL = "http://" + request.getServerName() + ":8080/okcert/rtn/house"; // 인증 완료 후 리턴될 URL (도메인 포함 full path)
			String RETURN_URL = "http://210.179.175.145/okcert/rtn/house"; // 인증 완료 후 리턴될 URL (도메인 포함 full path)

			// okcert3 요청 정보
			JSONObject reqJson = new JSONObject();
			reqJson.put("RETURN_URL", RETURN_URL);
			reqJson.put("SITE_NAME", SITE_NAME);
			reqJson.put("SITE_URL", SITE_URL);
			reqJson.put("RQST_CAUS_CD", RQST_CAUS_CD);
			reqJson.put("RETURN_MSG", quote_no);
			String reqStr = reqJson.toString();

			// okcert3 실행
			OkCert okcert = new OkCert();
			// String resultStr = okcert.callOkCert(TARGET_SYSTEM, CP_CD, svcName, LICENSE_FILE_NAME, reqStr);
			java.io.InputStream is = licenseResource.getInputStream(); // new java.io.FileInputStream(license);	// 환경에 맞게 InputStream 로드
			String resultStr = okcert.callOkCert(TARGET_SYSTEM, CP_CD, svcName, LICENSE_FILE_NAME, reqStr, is);
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
			data.put("popupUrl", POPUP_URL);
			data.put("CP_CD", CP_CD);
			data.put("MDL_TKN", MDL_TKN);
			data.put("RSLT_CD", RSLT_CD);
			data.put("RSLT_MSG", RSLT_MSG);
			data.put("succ", succ);

			log.info("okcert:{}", data.toString());
			return data;
		} catch (InsuAuthException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (InsuAuthExpiredException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.UPGRADE_REQUIRED, e.getMessage());
		}
	}

	// 아래 URL은 Filterconfig에서 Rule Out되어야 함.
	@PostMapping(path = "/rtn/house", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String rtn(HttpServletRequest request) throws OkCertException, IOException {
		String MDL_TKN = request.getParameter("mdl_tkn");

		// 서비스명 (고정값)
		String svcName = "IDS_HS_POPUP_RESULT";

		// okcert3 요청 정보
		JSONObject reqJson = new JSONObject();

		reqJson.put("MDL_TKN", MDL_TKN);
		String reqStr = reqJson.toString();

		// okcert3 실행
		OkCert okcert = new OkCert();

		// String resultStr = okcert.callOkCert(TARGET_SYSTEM, CP_CD, svcName, LICENSE_FILE_NAME, reqStr);

		java.io.InputStream is = licenseResource.getInputStream(); // new java.io.FileInputStream(license);	// 환경에 맞게 InputStream 로드
		String resultStr = okcert.callOkCert(TARGET_SYSTEM, CP_CD, svcName, LICENSE_FILE_NAME, reqStr, is);

		JSONObject resJson = new JSONObject(resultStr);

		String RSLT_CD = resJson.getString("RSLT_CD");
		String RSLT_MSG = resJson.getString("RSLT_MSG");
		String TX_SEQ_NO = resJson.getString("TX_SEQ_NO");

		String html = ""; // 응답 보낼 HTML
		if ("B000".equals(RSLT_CD)) {
			String RSLT_NAME = resJson.getString("RSLT_NAME");
			String RSLT_BIRTHDAY = resJson.getString("RSLT_BIRTHDAY");
			String RSLT_SEX_CD = resJson.getString("RSLT_SEX_CD");
			String RSLT_NTV_FRNR_CD = resJson.getString("RSLT_NTV_FRNR_CD");

			String DI = resJson.getString("DI");
			String CI = resJson.getString("CI");
			String CI_UPDATE = resJson.getString("CI_UPDATE");
			String TEL_COM_CD = resJson.getString("TEL_COM_CD");
			String TEL_NO = resJson.getString("TEL_NO");
			String RETURN_MSG = "";

			// RETURN_MSG 는 사용자가 요청 보낼 때 첨부하는 사용자 필드로 여기서는 quote_no가 들어간다.
			if (resJson.has("RETURN_MSG")) RETURN_MSG = resJson.getString("RETURN_MSG");

			// DB에 저장
			in007tMapper.delete(RETURN_MSG);
			in007tMapper.insert(RSLT_NAME, RSLT_BIRTHDAY, RSLT_SEX_CD, RSLT_NTV_FRNR_CD, DI, CI, CI_UPDATE, TEL_COM_CD, TEL_NO, RETURN_MSG);
			// 성공 시 클라이언트에 보낼 메세지
			html = "<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('ok');</script></html>";
		} else {
			// 실패 시 클라이언트에 보낼 메세지
			html = "<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('fail');</script></html>";
		}
		log.info("html:{}", html);
		return html;
	}
}
