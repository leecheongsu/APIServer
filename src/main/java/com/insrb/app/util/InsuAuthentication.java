package com.insrb.app.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.insrb.app.exception.InsuAuthException;
import com.insrb.app.exception.InsuAuthExpiredException;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Date;

// ref: https://github.com/auth0/java-jwt

public class InsuAuthentication {
    private static final String ID = "id";
    private static final String SECRET = "secret";
    private static final String ISS = "insurb.com";
    private static final String TOKEN_PREFIX = "Bearer";
    private static final String SPACE = " ";

    public static final String HEADER_STRING = "Authorization";

    
    /** 
     * @param email
     * @return String
     * @throws InsuAuthException
     */
    public static String GetAuthorizationValue(String email) throws InsuAuthException {
        return TOKEN_PREFIX + SPACE + CreateToken(email);
    }

    
    /** 
     * @param email
     * @return String
     * @throws InsuAuthException
     */
    public static String CreateToken(String email) throws InsuAuthException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            Date expiresAt = InsuDateUtil.Tomorrow();
            String token = JWT.create().withIssuer(ISS).withClaim(ID, email).withExpiresAt(expiresAt).sign(algorithm);
            return token;
        } catch (JWTCreationException | ParseException e) {
            throw new InsuAuthException(e.getMessage());
        }
    }

    
    /** 
     * @param email
     * @return String
     * @throws InsuAuthException
     */
    // For Test용
    public static String CreateYesterdayToken(String email) throws InsuAuthException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            Date expiresAt = InsuDateUtil.Yesterday();
            String token = JWT.create().withIssuer(ISS).withClaim(ID, email).withExpiresAt(expiresAt).sign(algorithm);
            return token;
        } catch (JWTCreationException | ParseException e) {
            throw new InsuAuthException(e.getMessage());
        }
    }

    
    /** 
     * @param token
     * @return String
     * @throws InsuAuthException
     * @throws InsuAuthExpiredException
     */
    public static String GetUserIdFromToken(String token) throws InsuAuthException, InsuAuthExpiredException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISS).build(); // Reusable
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim(ID).asString();
        } catch (TokenExpiredException e) {
            throw new InsuAuthExpiredException(e.getMessage());
        } catch (JWTVerificationException e) {
            throw new InsuAuthException(e.getMessage());
        }
    }

    
    /** 
     * 요청한 사용자와 토큰 상 사용자가 같은지 검증한다.
     * @param auth_header
     * @param user_id
     * @throws InsuAuthException
     * @throws InsuAuthExpiredException
     */
    public static void ValidateAuthHeader(String auth_header, String user_id) throws InsuAuthException, InsuAuthExpiredException {
        if (!StringUtils.hasText(auth_header))
            throw new InsuAuthException("no auth");

        String token = InsuAuthentication.GetTokenFromHeader(auth_header);
        String user_id_on_token = InsuAuthentication.GetUserIdFromToken(token);


        if (!user_id.equals(user_id_on_token))
            throw new InsuAuthException("user mismatch");

    }

    
    /** 
     * @param auth_header
     * @return String
     */
    public static String GetTokenFromHeader(String auth_header) {
        return auth_header.replace(TOKEN_PREFIX + " ", "");
    }
}