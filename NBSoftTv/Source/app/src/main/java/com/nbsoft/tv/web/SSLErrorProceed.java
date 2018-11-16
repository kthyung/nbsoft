package com.nbsoft.tv.web;

import android.webkit.SslErrorHandler;

public class SSLErrorProceed {
    public final String TAG = SSLErrorProceed.class.getSimpleName();

    private volatile static SSLErrorProceed uniqueInstance;
    private SslErrorHandler mSSLHandler;

    private SSLErrorProceed(SslErrorHandler handler){
        mSSLHandler = handler;
    }

    public static SSLErrorProceed getInstance(SslErrorHandler handler) {
        if (uniqueInstance == null) {
            uniqueInstance = new SSLErrorProceed(handler);
        }
        return uniqueInstance;
    }

    public void proceedHandler(){
        mSSLHandler.proceed();
        //mSSLHandler.cancel();
    }
}
