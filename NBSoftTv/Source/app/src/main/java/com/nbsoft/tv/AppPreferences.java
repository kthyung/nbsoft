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


    //구글 계정 이름
    public String getGoogleAccountName()						{ return mSharedPrefs.getString("googleAccountName", ""); }
    public String setGoogleAccountName(String googleAccountName)		{ return                   save("googleAccountName", googleAccountName); }

    //즐겨찾기 저장
    public String getYoutuberBookmark()						{ return mSharedPrefs.getString("youtuberBookmark", ""); }
    public String setYoutuberBookmark(String youtuberBookmark)		{ return                   save("youtuberBookmark", youtuberBookmark); }

    //재생기록 내역 저장
    public String getYoutuberHistory()						{ return mSharedPrefs.getString("youtuberHistory", ""); }
    public String setYoutuberHistory(String youtuberHistory)		{ return                   save("youtuberHistory", youtuberHistory); }

    //유투버 탭 저장
    public int getLastYoutuberType()						{ return mSharedPrefs.getInt("lastYoutuberType", 0); }
    public int setLastYoutuberType(int lastYoutuberType)		{ return                   save("lastYoutuberType", lastYoutuberType); }

    //3G/LTE 연결시 재생 허용
    public boolean get3gLteAccept()						{ return mSharedPrefs.getBoolean("lteAccept", true); }
    public boolean set3gLteAccept(boolean lteAccept)		{ return                   save("lteAccept", lteAccept); }

    //연속재생 여부 저장
    public boolean getAutoPlay()						{ return mSharedPrefs.getBoolean("autoPlay", true); }
    public boolean setAutoPlay(boolean autoPlay)		{ return                   save("autoPlay", autoPlay); }

    //재생기록 여부 저장
    public boolean getSaveHistory()						{ return mSharedPrefs.getBoolean("saveHistory", true); }
    public boolean setSaveHistory(boolean saveHistory)		{ return                   save("saveHistory", saveHistory); }

    //채널추가 신청 : 마지막 신청 시점
    public long getLastRequestTime()						{ return mSharedPrefs.getLong("lastRequestTime", 0L); }
    public long setLastRequestTime(long lastRequestTime)		{ return                   save("lastRequestTime", lastRequestTime); }

    //채널추가 신청 : 오늘 신청 횟수
    public int getRequestCount()						{ return mSharedPrefs.getInt("requestCount", 0); }
    public int setRequestCount(int requestCount)		{ return                   save("requestCount", requestCount); }

    //채널추가 신청 : 내역 저장
    public String getYoutuberRequest()						{ return mSharedPrefs.getString("youtuberRequest", ""); }
    public String setYoutuberRequest(String youtuberRequest)		{ return                   save("youtuberRequest", youtuberRequest); }

    //후원하기 : 마지막 후원 시점
    public long getShowAdTime()						        { return mSharedPrefs.getLong("showAdTime", 0L); }
    public long setShowAdTime(long showAdTime)		    { return                   save("showAdTime", showAdTime); }

    //후원하기 : 오늘 후원 횟수
    public int getShowAdCount()						        { return mSharedPrefs.getInt("showAdCount", 0); }
    public int setShowAdCount(int showAdCount)		        { return                   save("showAdCount", showAdCount); }

    //앱 현재버전
    public String getAppCurrentVersion()						{ return mSharedPrefs.getString("appCurrentVersion", ""); }
    public String setAppCurrentVersion(String appCurrentVersion)		{ return                   save("appCurrentVersion", appCurrentVersion); }

    //앱 최신버전
    public String getAppRecentVersion()						{ return mSharedPrefs.getString("appRecentVersion", ""); }
    public String setAppRecentVersion(String appRecentVersion)		{ return                   save("appRecentVersion", appRecentVersion); }

    //튜토리얼 여부 저장
    public boolean getShowTutorial()						{ return mSharedPrefs.getBoolean("showTutorial", true); }
    public boolean setShowTutorial(boolean showTutorial)		{ return                   save("showTutorial", showTutorial); }
}
