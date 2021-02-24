package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
public class InsuNumberUtilTest {

    @Test
    public void test_ToChar() {
        assertEquals("1,234,567,890", InsuNumberUtil.ToChar(1234567890));        
    }

    @Test
    public void test_ToIntChar() {
        assertEquals("35", InsuNumberUtil.ToIntChar(Double.parseDouble("35.0")));        
        assertEquals("35", InsuNumberUtil.ToIntChar("35.0"));        
        assertEquals("", InsuNumberUtil.ToIntChar("xcfsd"));        
    }

}
    