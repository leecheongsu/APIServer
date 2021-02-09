package com.insrb.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InsuDateUtil {

	public static java.util.Date ToDate(String str) throws ParseException {
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
		return transFormat.parse(str);
	}

	public static java.util.Date Today() throws ParseException {
		return new Date();
	}

	public static java.util.Date Tomorrow() throws ParseException {
		return new Date(Today().getTime() + (1000 * 60 * 60 * 24));
	}

	public static java.util.Date Yesterday() throws ParseException {
		return new Date(Today().getTime() - (1000 * 60 * 60 * 24));
	}

	public static java.util.Date GetOneYearPeriod(Date from) throws ParseException {
		Calendar cal =  Calendar.getInstance();
		cal.setTime(from);
		cal.add(Calendar.YEAR, 1); // to get previous year add 1
		cal.add(Calendar.DAY_OF_MONTH, -1); // to get previous day add -1
		Date oneYearLater = cal.getTime();
		return oneYearLater;
	}
}
