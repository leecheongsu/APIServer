package com.insrb.app.util.cyper;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import java.security.Key;

public class DESUtil {

    private static String tokenKey = "R5ZTtwUTQWIjc7t8dRkWew==";

    private static String secretKey = "ca2raJjwq8g9Lo8OIrjLtg==";

    public static Key getKey() throws Exception {
        return (secretKey.length() == 24)? getKey2(secretKey) : getKey1(secretKey);
    }

    public static Key getKey1(String keyValue) throws Exception {
        DESKeySpec desKeySpec = new DESKeySpec(keyValue.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        Key key = keyFactory.generateSecret(desKeySpec);
        return key;
    }

    public static Key getKey2(String keyValue) throws Exception {
        DESedeKeySpec desKeySpec = new DESedeKeySpec(keyValue.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        Key key = keyFactory.generateSecret(desKeySpec);
        return key;
    }

    public static String encrypt(String ID) throws Exception {
        if(ID == null || ID.length() == 0) {
            return "";
        }

        String instance = (secretKey.length() == 24) ? "DESede/ECB/PKCS5Padding" : "DES/ECB/PKCS5Padding";

        Cipher cipher = Cipher.getInstance(instance);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());

        String amalgam = ID;

        byte[] inputBytes1 = amalgam.getBytes("UTF8");

        byte[] outputBytes1 = cipher.doFinal(inputBytes1);

        BASE64Encoder encoder = new BASE64Encoder();
        String outputStr1 = encoder.encode(outputBytes1);
        return outputStr1;
    }

    public static String decrypt(String codedID) throws Exception {
        if(codedID == null || codedID.length() == 0) {
            return "";
        }

        String instance = (secretKey.length() == 24) ? "DESede/ECB/PKCS5Padding" : "DES/ECB/PKCS5Padding";

        Cipher cipher = Cipher.getInstance(instance);
        cipher.init(Cipher.DECRYPT_MODE, getKey());

        BASE64Decoder decoder = new BASE64Decoder();

        byte[] inputBytes1 = decoder.decodeBuffer(codedID);
        byte[] outputBytes2 = cipher.doFinal(inputBytes1);

        String strResult = new String(outputBytes2, "UTF8");

        return strResult;
    }

}
