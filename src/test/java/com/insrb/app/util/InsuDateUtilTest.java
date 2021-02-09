package com.insrb.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InsuDateUtilTest {

        
    @Test
    @DisplayName("20210101 의 1년 뒤는 20211231이여야 한다.")
    public void test_OneYearLater() throws ParseException {
        Date from = InsuDateUtil.ToDate("20210101");
        Date to = InsuDateUtil.GetOneYearPeriod(from);
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");

        assertEquals("20211231", transFormat.format(to));
    }
}
    