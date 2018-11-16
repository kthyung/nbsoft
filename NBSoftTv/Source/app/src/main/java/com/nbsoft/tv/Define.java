package com.nbsoft.tv;

public class Define {
    public static final String KEYSTORE_PASSWORD = "nbsoft";

    public static final String PROTOCOL_HTTP = "http://";
    public static final String SERVER_IP = "";
    //public static final String SERVER_IP = "125.141.204.170";
    public static final String SERVER_PORT_HTTP = "8080";

    public static final String PROTOCOL_YOUTUBE_HTTP = "https://";
    public static final String SERVER_YOUTUBE_IP = "www.googleapis.com/youtube/v3";
    public static final String SERVER_YOUTUBE_PORT = "";

    /**
	SSL api 주소
	 */
    public static final String GSTATIC_COM = "csi.gstatic.com";
    public static final String GOOGLE_COM = "google.com";
    public static final String GOOGLE_API_COM = "googleapis.com";
    public static final String FACEBOOK_COM = "facebook.com";
    public static final String FACEBOOK_NET = "facebook.net";
    public static final String FBCDN_NET = "fbcdn.net";
    public static final String FB_COM = "fb.com";

    /**
     * 로그인 타입
     */
    public static final int LOGIN_TYPE_NONE = 0;
    public static final int LOGIN_TYPE_FACEBOOK = 1;
    public static final int LOGIN_TYPE_GOOGLE = 2;
    public static final int LOGIN_TYPE_NAVER = 3;
    public static final int LOGIN_TYPE_KAKAO = 4;

    /**
     * Local Broadcast Action
     */
    public static final String ACTION_LOGIN_STATUS = "ACTION_LOGIN_STATUS";

    /**
     * Activity RequestCode
     */
    public static final int REQUEST_CODE_LOGIN = 1000;
    public static final int REQUEST_CODE_LOGIN_GOOGLE = 10000;
}
