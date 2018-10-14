package com.nbsoft.sample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.Define;
import com.nbsoft.sample.R;

import java.util.Arrays;

public class LoginActivity extends Activity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    private Context mContext;

    private LoginButton btn_facebook;
    private SignInButton btn_google;
    private Button btn_naver, btn_kakao;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private GoogleSignInClient mGoogleSignInClient;

    private AppPreferences mPreferences;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int loginType = mPreferences.getLoginType();
            switch (v.getId()){
                case R.id.btn_facebook:
                    if(loginType == Define.LOGIN_TYPE_NONE || loginType == Define.LOGIN_TYPE_FACEBOOK){
                        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
                    }
                    break;
                case R.id.btn_google:
                    if(loginType == Define.LOGIN_TYPE_NONE){
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, Define.REQUEST_CODE_LOGIN_GOOGLE);
                    }else if(loginType == Define.LOGIN_TYPE_GOOGLE){
                        mGoogleSignInClient.signOut()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "kth mGoogleSignInClient.signOut() onComplete()");
                                        mPreferences.setLoginType(Define.LOGIN_TYPE_NONE);
                                        setResult(RESULT_OK);
                                        setGooglePlusButtonText();
                                    }
                                });
                    }
                    break;
                case R.id.btn_naver:
                    if(loginType == Define.LOGIN_TYPE_NONE || loginType == Define.LOGIN_TYPE_NAVER){

                    }
                    break;
                case R.id.btn_kakao:
                    if(loginType == Define.LOGIN_TYPE_NONE || loginType == Define.LOGIN_TYPE_KAKAO){

                    }
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
        initGoogle();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Define.REQUEST_CODE_LOGIN_GOOGLE){
            Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = completedTask.getResult(ApiException.class);

                // Signed in successfully, show authenticated UI.
                Log.d(TAG, "kth onActivityResult() REQUEST_CODE_LOGIN_GOOGLE success. account.getServerAuthCode() : " + account.getServerAuthCode());
                mPreferences.setLoginType(Define.LOGIN_TYPE_GOOGLE);
                setResult(RESULT_OK);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.d(TAG, "kth onActivityResult() REQUEST_CODE_LOGIN_GOOGLE failed. e.getStatusCode() : " + e.getStatusCode());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            setGooglePlusButtonText();
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void initLayout(){
        btn_facebook = (LoginButton)findViewById(R.id.btn_facebook);
        btn_google = (SignInButton)findViewById(R.id.btn_google);
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

    private void initGoogle(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        setGooglePlusButtonText();
    }

    private void setGooglePlusButtonText() {
        String buttonText = "";

        int loginType = mPreferences.getLoginType();
        if(loginType == Define.LOGIN_TYPE_GOOGLE){
            buttonText = getString(R.string.logout);
        }else{
            buttonText = getString(R.string.login_google);
        }

        // Search all the views inside SignInButton for TextView
        for (int i = 0; i < btn_google.getChildCount(); i++) {
            View v = btn_google.getChildAt(i);

            // if the view is instance of TextView then change the text SignInButton
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }
}
