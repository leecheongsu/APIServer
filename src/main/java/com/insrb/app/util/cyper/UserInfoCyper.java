package com.insrb.app.util.cyper;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import com.insrb.app.exception.InsuEncryptException;

public class UserInfoCyper {
    private static String SALT = "insurobo";

    /**
     * 전화번호 / juminB(주민번호 뒷 6자리)는 양방향 암호화를 한다. 암호화 방식은 Triple DES, Rainbow Attack 을 피하기 위해 키 생성시
     * salt를 사용하는데, xml 파일 내용과 "insurobo" 를 합쳐 sha256으로 Hasing하고 그 결과 중 24 bytes를 키 값으로 사용. 이 키를
     * 사용하여 암.복호화한다.
     * 
     * @param data
     * @return String
     * @throws Exception
     */
    public static String EncryptMobile(String data) throws InsuEncryptException {
        Key key = TripleDESUtil.createKey(SALT);
        String enc = TripleDESUtil.encrypt(key, data);
        return enc;
    }


    public static String DecryptMobile(String data) throws InsuEncryptException {
        Key key = TripleDESUtil.createKey(SALT);
        String dec = TripleDESUtil.decrypt(key, data);
        return dec;
    }
 
    /**
     * 전화번호 암호화와 동일
     * 
     * @param juminB
     * @return String
     * @throws Exception
     */
    public static String EncryptJuminb(String juminB) throws InsuEncryptException {
        Key key = TripleDESUtil.createKey(SALT);
        return TripleDESUtil.encrypt(key, juminB);
    }

    /**
     * 비밀번호는 단방향 SHA256 Hashing을 사용한다. Rainbow Attack 을 피하기 위해 Hashing 시 이메일 주소를 salt로 사용한다.
     * 
     * @param emailAsSalt
     * @param plainPassword
     * @return String
     * @throws Exception
     */
    public static String EncryptPassword(String emailAsSalt, String plainPassword)
            throws InsuEncryptException {
        byte[] txt;
        try {
            txt = SHA256Util.hashAfterConcat(plainPassword, emailAsSalt);
        } catch (NoSuchAlgorithmException e) {
            throw new InsuEncryptException(e.getMessage());
        }
        return  Base64.getEncoder().encodeToString(txt);
    }

}