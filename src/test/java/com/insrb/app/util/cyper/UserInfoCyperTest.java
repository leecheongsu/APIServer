package com.insrb.app.util.cyper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserInfoCyperTest {

    @Test
    @DisplayName("전화번호암호화 결과가 DB::UserInfo에 저장된 값과 같아야한다")
    public void EncryptMobile1() throws Exception {
        assertEquals("09Pj5/iBuPTrGbBpQUjXQw==", UserInfoCyper.EncryptMobile("01047017956"));
    }
    
    @Test
    @DisplayName("전화번호암호화 결과가 DB::Adminuser에 저장된 값과 같아야한다")
    public void EncryptMobile2() throws Exception {
        assertEquals("jHUbmuYqgU/I847RNbmXhg==", UserInfoCyper.EncryptMobile("07041263333"));
    }

    @Test
    @DisplayName("주민번호 뒷6자리 암호화 결과가 DB::UserInfo에 저장된 값과 같아야한다")
    public void EncryptJuminB() throws Exception {
        assertEquals("sslYXfW6J5A=", UserInfoCyper.EncryptJuminb( "489319"));
    }


    @Test
    @DisplayName("패스워드암호화 결과가 DB::UserInfo에 저장된 값과 같아야한다")
    public void EncryptPassword() throws Exception {
        String emailAsSalt ="vingorius@gmail.com";
        String plainPassword = "vingo123!";
        assertEquals("/ZWAavIWEAYtXgzJLeAfI/RSqUhAxNZU+VjqxT95WYx2aW5nb3JpdXNAZ21haWwuY29t", UserInfoCyper.EncryptPassword(emailAsSalt,plainPassword));
    }

    @Test
    @DisplayName("패스워드암호화 결과가 DB::Adminuser에 저장된 값과 같아야한다")
    public void EncryptPassword2() throws Exception {
        String emailAsSalt ="insuroboad2019@insurobo.co.kr";
        String plainPassword = "239e522^";
        assertEquals("eVVFRotxp7yvBFwwR3mrU+ytKthCOMkUp7elDEns2cVpbnN1cm9ib2FkMjAxOUBpbnN1cm9iby5jby5rcg==", UserInfoCyper.EncryptPassword(emailAsSalt,plainPassword));
    }
}