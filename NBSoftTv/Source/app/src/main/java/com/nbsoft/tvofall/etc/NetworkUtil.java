package com.nbsoft.tvofall.etc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static final int NETWORK_DISCONNECTED = -1;
    public static final int NETWORK_CONNECTED_3GLTE = 0;
    public static final int NETWORK_CONNECTED_WIFI = 1;
    public static final int NETWORK_CONNECTED_ETC = 2;

    public static int networkStateCheck(Context mCtx) {
        ConnectivityManager cm = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        //기존엔 isConected로 되어 있었으나, Doze 모드 도입 후 isAvailable로 변경
        if (info == null || !info.isAvailable()) {
            return NETWORK_DISCONNECTED;
        }

        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return NETWORK_CONNECTED_WIFI;
            case ConnectivityManager.TYPE_MOBILE:
                return NETWORK_CONNECTED_3GLTE;
            default:
                return NETWORK_CONNECTED_ETC;
        }
    }
}
