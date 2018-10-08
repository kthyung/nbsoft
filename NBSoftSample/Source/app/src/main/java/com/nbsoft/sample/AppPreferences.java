package com.nbsoft.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppPreferences {
    public static final String TAG = AppPreferences.class.getSimpleName();

    private final static String NAME = "App_Preferences";
    private Context mContext;
    protected SharedPreferences mSharedPrefs;


    @SuppressLint("InlinedApi")
    public AppPreferences(Context context) {
        this.mContext = context;
        this.mSharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    protected float save(String key, float value) {
        Log.d(TAG, "kth save() key : " + key + " , value : " + value);
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.putFloat(key, value);
        edit.commit();

        return value;
    }

    protected long save(String key, long value) {
        Log.d(TAG, "kth save() key : " + key + " , value : " + value);
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.putLong(key, value);
        edit.commit();

        return value;
    }

    protected int save(String key, int value) {
        Log.d(TAG, "kth save() key : " + key + " , value : " + value);
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.putInt(key, value);
        edit.commit();

        return value;
    }

    protected String save(String key, String value) {
        Log.d(TAG, "kth save() key : " + key + " , value : " + value);
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.putString(key, value);
        edit.commit();

        return value;
    }

    protected boolean save(String key, boolean value) {
        Log.d(TAG, "kth save() key : " + key + " , value : " + value);
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.putBoolean(key, value);
        edit.commit();

        return value;
    }

    public void clear() {
        Log.e(TAG, "kth clear()");
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.clear();
        edit.commit();

        return;
    }


    public String getHelpInfo()						{ return mSharedPrefs.getString("helpInfo", ""); }
    public String setHelpInfo(String helpInfo)		{ return                   save("helpInfo", helpInfo); }
}
