package com.nbsoft.sample.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nbsoft.sample.view.LoadingPopupManager;

public class BaseWebView extends WebView {
    public static final String TAG = BaseWebView.class.getSimpleName();

    private final String mimetype = "text/html";
    private final String encoding = "UTF-8";

    private Context mContext;
    private Activity mActivity;
    private JSAndroidInterface mJsAndroidInterface;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseWebView(Context context) {
        super(context);
        this.mContext = context;

        initializeOptions();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        initializeOptions();
    }

    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setJSInterface(JSAndroidInterface jsInterface){
        this.mJsAndroidInterface = jsInterface;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initializeOptions() {
        setBackgroundColor(0);

        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDatabaseEnabled(true); 
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(this, true);
        }

        //settings.setSupportMultipleWindows(true);
        //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        //새 창 방지와 로딩 이벤트 제어
        setWebViewClient(new BaseWebViewClient());

        //setWebChromeClient(new WebChromeClient()
        //{
            /*public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg)
            {
                WebView newWebView = new WebView(mContext);
                newWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        window.setVisibility(View.GONE);
                        removeView(window);
                    }
                });
                addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }

            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
            {
                new AlertDialog.Builder(mContext)
                        .setTitle("AlertDialog")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            };*/

            /*@Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView newWebView = new WebView(mContext);
                WebSettings webSettings = newWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setLoadsImagesAutomatically(true);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setUseWideViewPort(true);
                webSettings.setDatabaseEnabled(true);
                webSettings.setDomStorageEnabled(true);

                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(newWebView); dialog.show();
                newWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        dialog.dismiss();
                    }
                });
                ((WebView.WebViewTransport)resultMsg.obj).setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }*/

        //});
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
    }

    public void setLayoutAlgorithm(WebSettings.LayoutAlgorithm value){
        getSettings().setLayoutAlgorithm(value);
    }

    public void loadUrl(String url) {
        super.loadUrl(url);
    }

    public void loadData(String data) {
        super.loadData(data, mimetype, encoding); 
    }

    public void loadDataWithBaseURL(String data) {
        super.loadDataWithBaseURL("", data, mimetype, encoding, ""); 
    }

    public boolean webCanGoBack() {
        if (this.canGoBack()) {
            try {
                this.goBack();
            } catch (Exception e) {

            }
            return true;
        }
        return false;
    }

    public class BaseWebViewClient extends WebViewClient {
        public final String TAG = BaseWebViewClient.class.getSimpleName();

        // 로딩이 시작될 때
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            Log.d(TAG, "kth onPageStarted() url : " + url);
            LoadingPopupManager.getInstance(mContext).showLoading(mActivity, true, "BaseWebView");
        }

        // 리소스를 로드하는 중 여러번 호출
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        // 방문 내역을 히스토리에 업데이트 할 때
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        // 로딩이 완료됬을 때 한번 호출
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.d(TAG, "kth onPageFinished() url : " + url);
            LoadingPopupManager.getInstance(mContext).hideLoading("BaseWebView");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            switch (errorCode) {
                case ERROR_AUTHENTICATION:      // 서버에서 사용자 인증 실패
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_AUTHENTICATION");
                    break;
                case ERROR_BAD_URL:             // 잘못된 URL
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_BAD_URL");
                    break;
                case ERROR_CONNECT:             // 서버로 연결 실패
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_CONNECT");
                    LoadingPopupManager.getInstance(mContext).hideLoading("BaseWebView");
                    break;
                case ERROR_FAILED_SSL_HANDSHAKE:    // SSL handshake 수행 실패
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_FAILED_SSL_HANDSHAKE");
                    break;
                case ERROR_FILE:                // 일반 파일 오류
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_FILE");
                    break;
                case ERROR_FILE_NOT_FOUND:      // 파일을 찾을 수 없습니다
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_FILE_NOT_FOUND");
                    break;
                case ERROR_HOST_LOOKUP:         // 서버 또는 프록시 호스트 이름 조회 실패
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_HOST_LOOKUP");
                    break;
                case ERROR_IO:                  // 서버에서 읽거나 서버로 쓰기 실패
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_IO");
                    break;
                case ERROR_PROXY_AUTHENTICATION:    // 프록시에서 사용자 인증 실패
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_PROXY_AUTHENTICATION");
                    break;
                case ERROR_REDIRECT_LOOP:       // 너무 많은 리디렉션
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_REDIRECT_LOOP");
                    break;
                case ERROR_TIMEOUT:             // 연결 시간 초과
                    LoadingPopupManager.getInstance(mContext).hideLoading("BaseWebView");
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_TIMEOUT");
                    break;
                case ERROR_TOO_MANY_REQUESTS:   // 페이지 로드중 너무 많은 요청 발생
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_TOO_MANY_REQUESTS");
                    break;
                case ERROR_UNKNOWN:             // 일반 오류
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_UNKNOWN");
                    break;
                case ERROR_UNSUPPORTED_AUTH_SCHEME: // 지원되지 않는 인증 체계
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_UNSUPPORTED_AUTH_SCHEME");
                    break;
                case ERROR_UNSUPPORTED_SCHEME:  // URI가 지원되지 않는 방식
                    Log.d(TAG, "kth onReceivedError() errorCode : ERROR_UNSUPPORTED_SCHEME");
                    break;
            }

        }

        // http 인증 요청이 있는 경우, 기본 동작은 요청 취소
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        // 확대나 크기 등의 변화가 있는 경우
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        // 잘못된 키 입력이 있는 경우
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        // 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 기회를 줌
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "kth shouldOverrideUrlLoading() url : " + url);
            try{
                if(!TextUtils.isEmpty(url)){
                    if(!url.contains("http") && !url.contains("https")){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        mContext.startActivity(intent);
                    }else{
                        view.loadUrl(url);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(TAG, "kth onReceivedSslError() error.toString() : " + error.toString());
            SSLErrorProceed.getInstance(handler).proceedHandler();
        }
    }
}
