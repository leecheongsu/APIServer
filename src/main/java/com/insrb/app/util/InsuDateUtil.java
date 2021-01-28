package com.insrb.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InsuDateUtil {
    public static java.util.Date toDate(String str) throws ParseException {
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
        return transFormat.parse(str);
    }

    public static java.util.Date today() throws ParseException {
        return new Date();
    }

}
