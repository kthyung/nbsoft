package com.nbsoft.sample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.R;
import com.nbsoft.sample.web.BaseWebView;
import com.nbsoft.sample.web.JSAndroidInterface;

public class ItemWebViewActivity extends Activity {
    public static final String TAG = ItemDetailActivity.class.getSimpleName();

    private Context mContext;

    private String mUrl;

    private BaseWebView mWebView;
    private JSAndroidInterface mJSAndroidInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_webview);

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

        mContext = this;

        Intent intent = getIntent();
        if(intent!=null){
            if(!intent.hasExtra("webUrl")){
                Log.d(TAG, "kth onCreate() webUrl is empty.");
                finish();
                return;
            }

            mUrl = intent.getStringExtra("webUrl");

            initLayout();
            loadWebView();
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView != null && mWebView.webCanGoBack()){
            return;
        }

        super.onBackPressed();
    }

    private void initLayout(){

    }

    private void loadWebView(){
        mWebView = (BaseWebView)findViewById(R.id.wv_content);
        mJSAndroidInterface = new JSAndroidInterface(this, mWebView);

        mWebView.setActivity(this);
        mWebView.setJSInterface(mJSAndroidInterface);
        mWebView.addJavascriptInterface(mJSAndroidInterface, "JSAndroidInterface");

        mWebView.loadUrl(mUrl);
    }
}
