package com.insrb.app.util.cyper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Util {

    /**
     * C# MD5DES.ComputeHash 소스를 자바로 구현한 것. 
     * 로직: return sha256(plainText + salt) + salt;
     */
    public static byte[] hashAfterConcat(String plainText, String salt) throws NoSuchAlgorithmException {
        byte[] msg = byte_concat(plainText.getBytes(), salt.getBytes());

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg);

        byte[] result = md.digest();
        return byte_concat(result, salt.getBytes());
    }

    /**
     * 두개의 byte array를 합쳐주는 util
     */
    static byte[] byte_concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

}
