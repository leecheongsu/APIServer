package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class InsuStringUtilTest {

    @Test
    public void test() {
        assertEquals(123, InsuStringUtil.ToIntOrDefault("123", 1));
        assertEquals(1, InsuStringUtil.ToIntOrDefault("abc", 1));
        assertEquals(1, InsuStringUtil.ToIntOrDefault(Integer.valueOf(1), 1));
        // assertEquals(123, 123);
    }
}
    