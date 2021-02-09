package com.insrb.app.util.cyper;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import com.insrb.app.exception.InsuEncryptException;

public class TripleDESUtil {


    /**
     * 암호화 방법
     */
    private static final String TRANSFORMATION = "DESede/ECB/PKCS5Padding";

    /**
     * TDES 키 생성 로직 : hash 값을 24 bytes(8bytes 씩 3개씩 사용하는 Triple DES 요건) 사용함. DESede : Triple DES 의
     * 자바 표현
     */
    public static Key createKey(final String salt) throws InsuEncryptException {

        byte[] hash;
        try {
            hash = SHA256Util.hashAfterConcat(Bookreader.reader(), salt);

            // System.out.println("TDESKey:" + new String(hash));
            final byte[] trimmedHash = new byte[24];
            for (int i = 0; i < trimmedHash.length; i++) {
                trimmedHash[i] = hash[i];
            }
            // System.out.println("trimmed TDESKey:" + new String(trimmedHash));

            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            final DESedeKeySpec desKeySpec = new DESedeKeySpec(trimmedHash);

            return keyFactory.generateSecret(desKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
            throw new InsuEncryptException(e.getMessage());
        }

    }

    /**
     * 문자열 대칭 암호화 return String 암호화된 문자열
     */
    public static String encrypt(final Key key, final String input) throws InsuEncryptException {

        if (input == null || input.length() == 0)
            return "";

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            final byte[] inputBytes = input.getBytes("UTF8");
            final byte[] outputBytes = cipher.doFinal(inputBytes);

            final Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(outputBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | UnsupportedEncodingException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new InsuEncryptException(e.getMessage());
        }
    }

    /**
     * 문자열 대칭 복호화 return String 복호화된 문자열
     */
    public static String decrypt(final Key key, final String input) throws InsuEncryptException {

        if (input == null || input.length() == 0)
            return "";

        try {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);

            final Decoder decoder = Base64.getDecoder();
            final byte[] inputBytes = decoder.decode(input);
            final byte[] outputBytes = cipher.doFinal(inputBytes);

            return new String(outputBytes, "UTF8");
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException
                | UnsupportedEncodingException e) {
            throw new InsuEncryptException(e.getMessage());
        }
    }
}
