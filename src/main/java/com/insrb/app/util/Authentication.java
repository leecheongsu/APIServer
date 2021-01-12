package com.insrb.app.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.insrb.app.exception.AuthException;
import org.springframework.util.StringUtils;

// ref: https://github.com/auth0/java-jwt

public class Authentication {
    private static final String ID = "id";

    private static final String SECRET = "secret";

    private static final String ISS = "insurb.com";

    private static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";

    public static String GetAuthorizationValue(String email) throws AuthException {
        return TOKEN_PREFIX + " " + GetToken(email);
    }

    public static String GetToken(String email) throws AuthException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            String token = JWT.create().withIssuer(ISS).withClaim(ID, email).sign(algorithm);
            return token;
        } catch (JWTCreationException e) {
            throw new AuthException(e.getMessage());
        }
    }


    public static String GetIdFromToken(String token) throws AuthException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISS).build(); // Reusable
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim(ID).asString();
        } catch (JWTVerificationException e) {
            throw new AuthException(e.getMessage());
        }
    }

    public static void ValidateAuthHeader(String auth_header, String id) throws AuthException {
        if (!StringUtils.hasText(auth_header))
            throw new AuthException("no auth");

        String token = Authentication.GetTokenFromHeader(auth_header);
        String token_id = Authentication.GetIdFromToken(token);


        if (!id.equals(token_id))
            throw new AuthException("user mismatch");

    }


    public static String GetTokenFromHeader(String auth_header) {
        return auth_header.replace(TOKEN_PREFIX + " ", "");
    }
}
