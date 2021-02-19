package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import com.insrb.app.util.cyper.AES256Ciper;
import com.insrb.app.util.cyper.SHA512Util;
import org.junit.jupiter.api.Test;
    
public class KGInisisUtilTest {

    @Test
    public void test() throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        assertEquals("9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043", SHA512Util.Hash("hello"));
		String KEY = "Igx2ofmG1L0BsWQ1";
		String IV = "blVTWJdw9IzrYw==";
		AES256Ciper aes = new AES256Ciper(KEY, IV);
        assertEquals("zqs3EfJdIkAbPUwUqPeCOg==", aes.encode("19681112"));
        // assertEquals("12", KGInisisUtil.card());
        // assertEquals("12", KGInisisUtil.vacct());
    }
}
