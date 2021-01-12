package com.insrb.app.mapper;

import java.util.HashMap;

public interface UserinfoMapper {

    String getCurrentDateTime();

    int insert(String email, String name, String teltype, String mobile, String pwd, String jumina,
            String sex, String utype);

    // Test에서 사용된 user 삭제할 때만 사용하고, 실재 웹 서비스는 하지 않는다.
    void delete(String email);

    HashMap<String, Object> selectById(String id);

    void selectAll(HashMap<String, Object> out);

    String findId(String name, String teltype, String mobile, String jumina, String sex);

    void updatePwd(String newPwd);

    void updateJuminb(String juminb);

    void updateBasic(String id, String name, String teltype, String mobile, String jumina,
            String sex);


}
