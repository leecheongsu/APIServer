package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.insrb.app.exception.BootpayException;
import org.junit.jupiter.api.Test;
import kong.unirest.json.JSONObject;
public class BootpayUtilTest {

    @Test
    public void test() throws BootpayException {
        String receipt_id = "6029cd90d8c1bd001ef759bc";
        assertEquals(true, BootpayUtil.GetToken() != null);
        JSONObject json = BootpayUtil.ValidateReceipt(receipt_id);
        assertEquals(receipt_id,json.getString("receipt_id") );
    }
}