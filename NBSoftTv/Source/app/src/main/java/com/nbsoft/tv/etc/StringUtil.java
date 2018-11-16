package com.nbsoft.tv.etc;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class StringUtil {

    /**
     * 마지막 문자 체크
     * @param search
     * @param source
     * @return
     */
    public static boolean lastStringCheck(String search, String source){
        boolean result = false;
        String temp = source;
        String lastString = temp.substring(temp.length()-1);
        if(lastString.equals(search)){
            result = true;
        }
        return result;
    }

    public static String getFormatedNumber(String input){
        String result = input;
        try {
            long longNumber = Long.parseLong(input);
            result = NumberFormat.getNumberInstance(Locale.KOREAN).format(longNumber);
        } catch (Exception e) {
            e.printStackTrace();
            result = input;
        }

        return result;
    }
}
