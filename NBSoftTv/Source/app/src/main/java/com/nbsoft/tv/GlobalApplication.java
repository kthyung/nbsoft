package com.nbsoft.tv;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class GlobalApplication extends Application {
    private static GlobalApplication mInstance;

    public static GlobalApplication getGlobalApplicationContext() {
        if(mInstance == null)
            throw new IllegalStateException("this application does not inherit GlobalApplication");
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        //NukeSSLCerts.nuke();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        mInstance = null;
    }
}



