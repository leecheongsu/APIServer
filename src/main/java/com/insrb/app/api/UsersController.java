package com.insrb.app.api;

import java.util.Map;
import java.util.Objects;
import com.insrb.app.exception.AuthException;
import com.insrb.app.exception.EncryptException;
import com.insrb.app.mapper.GaDetailsMapper;
import com.insrb.app.mapper.UserinfoMapper;
import com.insrb.app.util.Authentication;
import com.insrb.app.util.cyper.UserInfoCyper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
// @SuppressWarnings("unchecked")
public class UsersController {

    @Autowired
    UserinfoMapper UserinfoMapper;
    @Autowired
    GaDetailsMapper GaDetailMapper;

    // @GetMapping(path = "today")
    // public String today() {
    // log.info("Today is called");
    // return UserinfoMapper.getCurrentDateTime();
    // }

    // Example For procedure's cursor
    // @GetMapping(path = "/all")
    // public List<HashMap<String, Object>> selectAll() {
    // HashMap<String, Object> param = new HashMap<String, Object>();
    // UserinfoMapper.selectAll(param);
    // return (List<HashMap<String, Object>>) param.get("p_cursor");
    // }

    @PostMapping(path = "")
    public void insert(@RequestParam(name = "email", required = true) String email,
            @RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "teltype", required = true) String teltype,
            @RequestParam(name = "mobile", required = true) String mobile,
            @RequestParam(name = "pwd", required = true) String pwd,
            @RequestParam(name = "jumina", required = true) String jumina,
            @RequestParam(name = "sex", required = true) String sex) {

        String utype = "u";
        try {
            Integer.parseInt(jumina); // isValidNumber
            Integer.parseInt(mobile); // isValidNumber
            Integer.parseInt(sex); // isValidNumber
            String encMobile = UserInfoCyper.EncryptMobile(mobile);
            String encPwd = UserInfoCyper.EncryptPassword(email, pwd);
            int result = UserinfoMapper.insert(email, name, teltype, encMobile, encPwd, jumina, sex,
                    utype);
            if (result < 1)
                throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid juminb");
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping(path = "business")
    public void business(@RequestParam(name = "email", required = true) String email,
            @RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "teltype", required = true) String teltype,
            @RequestParam(name = "mobile", required = true) String mobile,
            @RequestParam(name = "pwd", required = true) String pwd,
            @RequestParam(name = "jumina", required = true) String jumina,
            @RequestParam(name = "sex", required = true) String sex,
            @RequestParam(name = "comname", required = true) String comname,
            @RequestParam(name = "sosok", required = true) String sosok,
            @RequestParam(name = "businessnum", required = true) String businessnum) {

        String utype = "GA";
        try {
            Integer.parseInt(jumina); // isValidNumber
            Integer.parseInt(mobile); // isValidNumber
            Integer.parseInt(sex); // isValidNumber
            Integer.parseInt(businessnum); // isValidNumber
            String encMobile = UserInfoCyper.EncryptMobile(mobile);
            String encPwd = UserInfoCyper.EncryptPassword(email, pwd);
            int result = UserinfoMapper.insert(email, name, teltype, encMobile, encPwd, jumina, sex,
                    utype);
            if (result < 1)
                throw new ResponseStatusException(HttpStatus.NO_CONTENT);
            GaDetailMapper.merge(email, comname, sosok, businessnum);

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid juminb");
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping(path = "/{id}")
    public Map<String, Object> selectById(@PathVariable String id) {
        Map<String, Object> user = UserinfoMapper.selectById(id);
        if (Objects.isNull(user))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return user;
    }

    @GetMapping(path = "/{id}/isjoined")
    public String isjoined(@PathVariable String id) {
        Map<String, Object> user = UserinfoMapper.selectById(id);
        if (Objects.isNull(user))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return (String) user.get("email");
    }

    @PostMapping(path = "/auth")
    public Map<String, Object> auth(@RequestParam(name = "id", required = true) String id,
            @RequestParam(name = "pwd", required = true) String plainPassword) {

        Map<String, Object> user = UserinfoMapper.selectById(id);

        if (Objects.isNull(user))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String pwdOnDatabase = (String) user.get("pwd");

        try {
            if (!pwdOnDatabase.equals(UserInfoCyper.EncryptPassword(id, plainPassword))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid pwd");
            } else {
                user.remove("pwd");
                user.put("token", Authentication.GetToken(id));
                return user;
            }
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (AuthException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @GetMapping(path = "/email")
    public String email(@RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "teltype", required = true) String teltype,
            @RequestParam(name = "mobile", required = true) String mobile,
            @RequestParam(name = "jumina", required = true) String jumina,
            @RequestParam(name = "sex", required = true) String sex) {

        try {
            String encMobile = UserInfoCyper.EncryptMobile(mobile);
            String email = UserinfoMapper.findId(name, teltype, encMobile, jumina, sex);
            if (Objects.isNull(email))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no user");
            return email;
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping(path = "/{id}/can-change")
    public String canChange(@PathVariable String id,
            @RequestParam(name = "teltype", required = true) String teltype,
            @RequestParam(name = "mobile", required = true) String mobile) {
        Map<String, Object> user = UserinfoMapper.selectById(id);

        if (Objects.isNull(user))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no user");

        try {
            String encMobile = UserInfoCyper.EncryptMobile(mobile);
            if (user.get("teltype").equals(teltype) && user.get("mobile").equals(encMobile))
                return "OK";
            else
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid user info");
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping(path = "/{id}/pwd")
    public String pwd(@PathVariable String id,
            @RequestParam(name = "teltype", required = true) String teltype,
            @RequestParam(name = "mobile", required = true) String mobile,
            @RequestParam(name = "newPwd", required = true) String newPwd) {
        Map<String, Object> user = UserinfoMapper.selectById(id);

        if (Objects.isNull(user))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        try {
            String encMobile = UserInfoCyper.EncryptMobile(mobile);
            if (user.get("teltype").equals(teltype) && user.get("mobile").equals(encMobile)) {
                String encPwd = UserInfoCyper.EncryptPassword(id, newPwd);
                UserinfoMapper.updatePwd(encPwd);
                return "OK";
            } else
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "mismatch");
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @PutMapping(path = "/{id}/juminb")
    public String juminb(
            @RequestHeader(name = "Authorization", required = false) String auth_header,
            @PathVariable String id,
            @RequestParam(name = "juminb", required = true) String juminb) {

        try {
            Authentication.ValidateAuthHeader(auth_header, id);
            Integer.parseInt(juminb); // isValidNumber
            String encJuminb = UserInfoCyper.EncryptJuminb(juminb);

            UserinfoMapper.updateJuminb(encJuminb);
            return "OK";
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid juminb");
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

    }


    @PutMapping(path = "/{id}/basic")
    public String basic(@RequestHeader(name = "Authorization", required = false) String auth_header,
            @PathVariable String id, @RequestParam(name = "name", required = true) String name,
            @RequestParam(name = "teltype", required = true) String teltype,
            @RequestParam(name = "mobile", required = true) String mobile,
            @RequestParam(name = "jumina", required = true) String jumina,
            @RequestParam(name = "sex", required = true) String sex) {

        try {
            Authentication.ValidateAuthHeader(auth_header, id);

            Integer.parseInt(mobile); // isValidNumber
            Integer.parseInt(jumina); // isValidNumber
            String encMobile = UserInfoCyper.EncryptMobile(mobile);
            UserinfoMapper.updateBasic(id, name, teltype, encMobile, jumina, sex);
            return "OK";
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid number");
        } catch (EncryptException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

    }

    @PutMapping(path = "/{id}/business")
    public String business(
            @RequestHeader(name = "Authorization", required = false) String auth_header,
            @PathVariable String id,
            @RequestParam(name = "comname", required = true) String comname,
            @RequestParam(name = "sosok", required = true) String sosok,
            @RequestParam(name = "businessnum", required = true) String businessnum) {
        try {
            Authentication.ValidateAuthHeader(auth_header, id);

            Integer.parseInt(businessnum); // isValidNumber
            GaDetailMapper.merge(id, comname, sosok, businessnum);
            return "OK";
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid number");
        } catch (AuthException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

}
