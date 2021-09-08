package com.insrb.app.api;

import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuEncryptException;
import com.insrb.app.mapper.RIN005TMapper;
import com.insrb.app.util.InsuAuthentication;
import com.insrb.app.util.cyper.UserInfoCyper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;

@Slf4j
@RestController
@RequestMapping("/sociallogin")
public class SocialLogincontroller {

//    private String KAKAO_REDIRECT_URI = "http://localhost:8080";
    private String KAKAO_REDIRECT_URI = "https://insrb.com";

    private String KAKAO_REST_API_KEY = "1a8b73bd5fde3e644891dc90e99012b5";

    private String KAKAO_CLIENT_SECRET = "nsZvEiu7bJyquUQgM2JgVOr1q6tdjDHj";

    private String NAVER_CLIENT_ID = "Eu2ZshOPX0OesLWrb3vG";
    private String NAVER_CLIENT_SECRET = "qhoi7d33gt";

    @Autowired
    RIN005TMapper rin005TMapper;

    @RequestMapping(
            path = "/login/rtn_kakao",
            method = { RequestMethod.POST, RequestMethod.GET }
    )
    @ResponseBody
    public String rtn_kakao(HttpServletRequest request) throws IOException, InsuAuthException {
            String permission_code = request.getQueryString().substring(5);

            try{

                //  [ 인가받기 ]는 APPLICATION 에서 완료.
                // 2. 토큰받기
                HttpResponse<String> response_token = Unirest
                        .post("https://kauth.kakao.com/oauth/token")
                        .queryString("grant_type", "authorization_code")
                        .queryString("client_id", KAKAO_REST_API_KEY)
                        .queryString("redirect_uri", KAKAO_REDIRECT_URI + "/sociallogin/login/rtn_kakao")
                        .queryString("code", permission_code)
                        .queryString("client_secret", KAKAO_CLIENT_SECRET)
                        .asString();


                JSONObject token = new JSONObject(response_token.getBody());
                String auth_toUserInfo ="Bearer " + token.getString("access_token").toString();


                // 3. 사용자정보가져오기
                HttpResponse<String> response_info = Unirest
                        .get("https://kapi.kakao.com/v2/user/me")
                        .header("Authorization", auth_toUserInfo)
                        .asString();

                JSONObject userInfo = new JSONObject(response_info.getBody());

                //카카오 - 닉네임, 이메일, 성별, 생일, 출생연도, 전화번호 : 닉네임을 이름으로 간주
                String nickname, email, birthyear, birthday, gender, birth, mobile, auth_header, flag = null;
                String temp = null;

                nickname = userInfo.getJSONObject("kakao_account").getJSONObject("profile").getString("nickname");
                email = userInfo.getJSONObject("kakao_account").getString("email");
                gender = userInfo.getJSONObject("kakao_account").getString("gender");

                birthyear = userInfo.getJSONObject("kakao_account").getString("birthyear");
                birthday = userInfo.getJSONObject("kakao_account").getString("birthday");

                birth = userInfo.getJSONObject("kakao_account").getString("birthyear").substring(2)+userInfo.getJSONObject("kakao_account").getString("birthday");

                temp = "0" + userInfo.getJSONObject("kakao_account").getString("phone_number").substring(4);
                mobile = temp.replace("-", "");

                //토큰 생성
                auth_header = InsuAuthentication.CreateToken(email);

                flag = "kakao";

                String html =  "<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('ok/"
                        +nickname+"/"
                        +email+"/"
                        +mobile+"/"
                        +birth+"/"
                        +auth_header+"/"
                        +gender+"/"
                        +birthyear+"/"
                        +birthday+"/"
                        +flag+"/"
                        +"');</script></html>";
                return html;
            }catch (Exception e){
                log.debug(e.getMessage());
                return "fail";
            }
    }

    @RequestMapping(
            path = "/login/rtn_naver",
            method = { RequestMethod.POST, RequestMethod.GET }
    )

    @ResponseBody
    public String rtn_naver(HttpServletRequest request) throws IOException, InsuAuthException {

        String permission_code = request.getParameter("code");

        try{

            HttpResponse<String> postData = Unirest
                    .post("https://nid.naver.com/oauth2.0/token")
                    .queryString("grant_type", "authorization_code")
                    .queryString("client_id", NAVER_CLIENT_ID)
                    .queryString("client_secret", NAVER_CLIENT_SECRET)
                    .queryString("code", permission_code)
                    .queryString("state", "state_string")
                    .asString();

            JSONObject data = new JSONObject(postData.getBody());
            String access_token = "Bearer " + data.getString("access_token");

            HttpResponse<String> getData = Unirest
                    .get("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", access_token)
                    .asString();

            JSONObject user_data = new JSONObject(getData.getBody());

            String nickname = user_data.getJSONObject("response").getString("nickname");
            String gender = user_data.getJSONObject("response").getString("gender");
            String email = user_data.getJSONObject("response").getString("email");
            String mobile = user_data.getJSONObject("response").getString("mobile").replace("-", "");
            String name = user_data.getJSONObject("response").getString("name");
            String birthyear = user_data.getJSONObject("response").getString("birthyear");
            String birthday = user_data.getJSONObject("response").getString("birthday").replaceAll("-", "");
            String birth = user_data.getJSONObject("response").getString("birthyear").substring(2)+user_data.getJSONObject("response").getString("birthday").replace("-", "");
            String flag = "naver";

            String auth_header = InsuAuthentication.CreateToken(email);

            String html =  "<html><title></title><body></body><script>window.ReactNativeWebView.postMessage('ok/"
                    +nickname+"/"
                    +email+"/"
                    +mobile+"/"
                    +name+"/"
                    +birth+"/"
                    +auth_header+"/"
                    +gender+"/"
                    +birthyear+"/"
                    +birthday+"/"
                    +flag+"/"
                    +"');</script></html>";
            return html;
        }catch (Exception e){
            log.debug(e.getMessage());
            return "fail";
        }
    }

    @PostMapping(path = "join")
    public String socialJoin(
        @RequestParam(name = "email", required=true) String email,
        @RequestParam(name = "mobile", required = true) String mobile,
        @RequestParam(name = "name", required = true) String name,
        @RequestParam(name = "gender", required = true) String gender,
        @RequestParam(name = "birthyear", required = true) String birthyear,
        @RequestParam(name = "birthday", required = true) String birthday,
        @RequestParam(name = "flag", required = true) String flag
    ) {
        String utype = "u";
        try{
            log.debug(email+mobile+name+gender+birthyear+birthday+flag);
            Integer.parseInt(birthyear); // isValidNumber
            Integer.parseInt(mobile); // isValidNumber
            Integer.parseInt(birthday); // isValidNumber
            String encMobile = UserInfoCyper.EncryptMobile(mobile);

            int result = rin005TMapper.insert(email, name, encMobile, birthyear, birthday, gender, utype, flag);
            if(result < 1) throw new ResponseStatusException(HttpStatus.NO_CONTENT);
            return "ok";
        } catch (InsuEncryptException e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "암호화 오류.");
        } catch (DataAccessException e) {
            log.debug(e.getMessage());
            if (e.getRootCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) e.getRootCause();
                int sqlErrorCode = sqlEx.getErrorCode();
                log.debug("sqlErrorCode:" + sqlErrorCode);
                if (sqlErrorCode == -10007) { // Unique Constriant Error
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "로그인 완료");
                }
            }
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }



}
