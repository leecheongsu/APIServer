package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
public class InsuNumberUtilTest {

    @Test
    public void test() {
        assertEquals("1,234,567,890", InsuNumberUtil.ToChar(1234567890));        
    }
}
    