package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.insrb.app.exception.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class AuthenticationTest {
    private String email = "vingorius@gmail.com";

    @Test
    @DisplayName("인증 토큰으로 부터 id(email)을 가져와야 한다.")
    public void test02_varifyToken() throws AuthException {
        String token = (String) Authentication.GetToken(email);
        assertEquals(email, Authentication.GetIdFromToken(token));
    }
}
    