package com.nbsoft.sample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.Define;
import com.nbsoft.sample.R;

import java.util.Arrays;

public class LoginActivity extends Activity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    private Context mContext;

    private LoginButton btn_facebook;
    private Button btn_google, btn_naver, btn_kakao;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private AppPreferences mPreferences;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_facebook:
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
                    break;
                case R.id.btn_google:
                    break;
                case R.id.btn_naver:
                    break;
                case R.id.btn_kakao:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

        mContext = this;

        mPreferences = new AppPreferences(mContext);

        initLayout();
        initFacebook();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void initLayout(){
        btn_facebook = (LoginButton)findViewById(R.id.btn_facebook);
        btn_google = (Button)findViewById(R.id.btn_google);
        btn_naver = (Button)findViewById(R.id.btn_naver);
        btn_kakao = (Button)findViewById(R.id.btn_kakao);

        btn_facebook.setOnClickListener(onClickListener);
        btn_google.setOnClickListener(onClickListener);
        btn_naver.setOnClickListener(onClickListener);
        btn_kakao.setOnClickListener(onClickListener);
    }

    private void initFacebook(){
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d(TAG, "kth AccessTokenTracker onCurrentAccessTokenChanged() oldAccessToken.getToken() : " + (oldAccessToken!=null ? oldAccessToken.getToken() : ""));
                Log.d(TAG, "kth AccessTokenTracker onCurrentAccessTokenChanged() currentAccessToken.getToken() : " + (currentAccessToken!=null ? currentAccessToken.getToken() : ""));
                if (currentAccessToken != null){
                    //User logged in
                    mPreferences.setLoginType(Define.LOGIN_TYPE_FACEBOOK);
                    setResult(RESULT_OK);
                }else{
                    //User logged out
                    mPreferences.setLoginType(Define.LOGIN_TYPE_NONE);
                    setResult(RESULT_OK);
                }
            }
        };

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d(TAG, "kth FacebookCallback onSuccess() loginResult.getAccessToken() : " + loginResult.getAccessToken());
                        Log.d(TAG, "kth FacebookCallback onSuccess() loginResult.getRecentlyDeniedPermissions() : " + loginResult.getRecentlyDeniedPermissions());
                        Log.d(TAG, "kth FacebookCallback onSuccess() loginResult.getRecentlyGrantedPermissions() : " + loginResult.getRecentlyGrantedPermissions());

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d(TAG, "kth FacebookCallback onError() exception.getStackTrace() : " + exception.getStackTrace());
                        Log.d(TAG, "kth FacebookCallback onError() exception.getMessage() : " + exception.getMessage());
                        Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
