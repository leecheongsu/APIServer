package com.insrb.app.util;

import java.text.NumberFormat;

public class InsuNumberUtil {

	public static String ToChar(int number) {
		return NumberFormat.getInstance().format(number);
	}

	public static String ToChar(long number) {
		return NumberFormat.getInstance().format(number);
	}
}
