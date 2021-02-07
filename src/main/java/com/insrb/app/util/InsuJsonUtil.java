package com.insrb.app.util;

public class InsuJsonUtil {

  public static double IntOrDoubleToDouble(Object item) {
  	if (item instanceof Integer) {
  		return (int) item;
  	} else if (item instanceof Double) {
  		return (double) item;
  	}
  	return 0.0;
  }
    
}
