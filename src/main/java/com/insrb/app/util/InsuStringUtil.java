package com.insrb.app.util;

public class InsuStringUtil {
    public static boolean isEmpty(String str){
        if(str == null) return true;
        if(str.trim().length() < 1) return true;
        else return false;
    }

    public static boolean equals(String str1,String str2){
        if(str1 == null) return false;
        if(str2 == null) return false;
        if(str1.equals(str2)) return true;
        else return false;
    }
}
