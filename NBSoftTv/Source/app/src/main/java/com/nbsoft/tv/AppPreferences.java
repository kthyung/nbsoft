package com.nbsoft.tv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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


    public String getGoogleAccountName()						{ return mSharedPrefs.getString("googleAccountName", ""); }
    public String setGoogleAccountName(String googleAccountName)		{ return                   save("googleAccountName", googleAccountName); }

    public String getYoutuberBookmark()						{ return mSharedPrefs.getString("youtuberBookmark", ""); }
    public String setYoutuberBookmark(String youtuberBookmark)		{ return                   save("youtuberBookmark", youtuberBookmark); }

    public String getYoutuberHistory()						{ return mSharedPrefs.getString("youtuberHistory", ""); }
    public String setYoutuberHistory(String youtuberHistory)		{ return                   save("youtuberHistory", youtuberHistory); }

    public int getLastYoutuberType()						{ return mSharedPrefs.getInt("lastYoutuberType", 0); }
    public int setLastYoutuberType(int lastYoutuberType)		{ return                   save("lastYoutuberType", lastYoutuberType); }

    public boolean getAutoPlay()						{ return mSharedPrefs.getBoolean("autoPlay", true); }
    public boolean setAutoPlay(boolean autoPlay)		{ return                   save("autoPlay", autoPlay); }
}
