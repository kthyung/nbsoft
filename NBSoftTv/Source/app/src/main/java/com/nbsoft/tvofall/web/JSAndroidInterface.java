package com.nbsoft.tvofall.web;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class JSAndroidInterface {
    public static final String TAG = JSAndroidInterface.class.getSimpleName();

    private Context mContext;
    private WebView mWebview;

    public JSAndroidInterface(final Context context, final WebView wv){
        mContext = context;
        mWebview = wv;
    }

    @JavascriptInterface
    public void showToast(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
