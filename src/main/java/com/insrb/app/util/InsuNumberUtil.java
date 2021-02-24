package com.insrb.app.util;

import java.text.NumberFormat;

public class InsuNumberUtil {

	public static String ToChar(int number) {
		return NumberFormat.getInstance().format(number);
	}

	public static String ToChar(long number) {
		return NumberFormat.getInstance().format(number);
	}

	public static String ToIntChar(Object obj) {
		String str = "";
		if (obj instanceof Double) str = String.valueOf(((Double) obj).intValue());
		if (obj instanceof Integer) str = String.valueOf(((Integer) obj).intValue());
		if (obj instanceof String) {
			try {
				str = String.valueOf(Integer.parseInt((String) obj));
			} catch (NumberFormatException e) {
				try {
					str = String.valueOf(Math.round(Double.parseDouble((String) obj)));
				} catch (NumberFormatException e2) {
					return "";
				}
			}
		}
		return str;
	}
}
