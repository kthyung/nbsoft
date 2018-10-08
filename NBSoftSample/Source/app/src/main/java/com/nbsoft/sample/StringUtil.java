package com.nbsoft.sample;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Pattern;

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
}
