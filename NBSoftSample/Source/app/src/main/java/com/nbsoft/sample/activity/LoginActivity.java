package com.nbsoft.sample.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.Define;
import com.nbsoft.sample.GlobalApplication;
import com.nbsoft.sample.R;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    private Context mContext;

    private com.facebook.login.widget.LoginButton btn_facebook;
    private SignInButton btn_google;
    private OAuthLoginButton btn_naver;
    private Button btn_kakao;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private GoogleSignInClient mGoogleSignInClient;

    private OAuthLogin oAuthLogin = OAuthLogin.getInstance();

    private SessionCallback mKakaoSessionCallback;

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
                    if(loginType == Define.LOGIN_TYPE_NONE){
                        oAuthLogin.startOauthLoginActivity(LoginActivity.this, mNaverOAuthHandler);
                    }else if(loginType == Define.LOGIN_TYPE_NAVER){
                        logoutWithNaver();
                    }
                    break;
                case R.id.btn_kakao:
                    if(loginType == Define.LOGIN_TYPE_NONE){
                        Session.getCurrentSession().checkAndImplicitOpen();
                        Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);
                    }else if(loginType == Define.LOGIN_TYPE_KAKAO){
                        UserManagement.requestLogout(new LogoutResponseCallback() {
                            @Override
                            public void onCompleteLogout() {
                                Log.d(TAG, "kth UserManagement.requestLogout() onCompleteLogout()");
                                mPreferences.setLoginType(Define.LOGIN_TYPE_NONE);
                                setResult(RESULT_OK);

                                setKakaoButtonText();
                            }
                        });
                    }
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private OAuthLoginHandler mNaverOAuthHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                Log.d(TAG, "kth OAuthLoginHandler run() success. " +
                        " accessToken : " + oAuthLogin.getAccessToken(mContext));
                mPreferences.setLoginType(Define.LOGIN_TYPE_NAVER);
                setResult(RESULT_OK);

                getNaverProfile();
            }else{
                Log.d(TAG, "kth OAuthLoginHandler run() failed. " +
                        " ErrorCode : " + oAuthLogin.getLastErrorCode(mContext) +
                        " ErrorDesc : " + oAuthLogin.getLastErrorDesc(mContext));
                Toast.makeText(mContext, oAuthLogin.getLastErrorDesc(mContext), Toast.LENGTH_SHORT).show();
            }

            setNaverButtonText();
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
        initNaver();
        initKakao();
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
            if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
                return;
            }

            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        Session.getCurrentSession().removeCallback(mKakaoSessionCallback);
    }

    private void initLayout(){
        btn_facebook = (com.facebook.login.widget.LoginButton)findViewById(R.id.btn_facebook);
        btn_google = (SignInButton)findViewById(R.id.btn_google);
        btn_naver = (OAuthLoginButton)findViewById(R.id.btn_naver);
        btn_kakao = (Button)findViewById(R.id.btn_kakao);

        btn_facebook.setOnClickListener(onClickListener);
        btn_google.setOnClickListener(onClickListener);
        btn_naver.setOnClickListener(onClickListener);
        btn_kakao.setOnClickListener(onClickListener);

        btn_naver.setOAuthLoginHandler(mNaverOAuthHandler);
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

    private void initNaver(){
        setNaverButtonText();
    }

    private void setNaverButtonText() {
        btn_naver.post(new Runnable() {
            @Override
            public void run() {
                int resId = R.drawable.naver_login;

                int loginType = mPreferences.getLoginType();
                if(loginType == Define.LOGIN_TYPE_NAVER){
                    resId = R.drawable.naver_logout;
                }else{
                    resId = R.drawable.naver_login;
                }

                btn_naver.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                btn_naver.setImageResource(resId);
            }
        });
    }

    private void logoutWithNaver(){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Void> execute = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                boolean result = oAuthLogin.logoutAndDeleteToken(mContext);
                Log.d(TAG, "kth logoutWithNaver() result. " + result);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mPreferences.setLoginType(Define.LOGIN_TYPE_NONE);
                setResult(RESULT_OK);
                setNaverButtonText();
            }
        }.execute();
    }

    private void getNaverProfile(){
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, StringBuffer> execute = new AsyncTask<Void, Void, StringBuffer>() {
            @Override
            protected StringBuffer doInBackground(Void... voids) {
                StringBuffer response = null;
                try {
                    String token = oAuthLogin.getAccessToken(mContext);// 네이버 로그인 접근 토큰;
                    String header = "Bearer " + token; // Bearer 다음에 공백 추가

                    String apiURL = "https://openapi.naver.com/v1/nid/me";
                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Authorization", header);
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    if(responseCode == 200) { // 정상 호출
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                        String inputLine;
                        response = new StringBuffer();
                        while ((inputLine = br.readLine()) != null) {
                            response.append(inputLine);
                        }
                        br.close();
                    } else {  // 에러 발생

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;
            }

            @Override
            protected void onPostExecute(StringBuffer buffer) {
                super.onPostExecute(buffer);
                if(buffer != null){
                    Log.d(TAG, "kth getNaverProfile() success. buffer : " + buffer.toString());
                    try {
                        JSONObject jsonObj = new JSONObject(buffer.toString());
                        String resultcode = jsonObj.getString("resultcode");
                        if(resultcode!=null && resultcode.equals("00")){
                            JSONObject response = jsonObj.getJSONObject("response");
                            if(response != null){
                                String profile_image = response.getString("profile_image");
                                String email = response.getString("email");
                                String name = response.getString("name");

                                mPreferences.setNaverProfileName(name);
                                mPreferences.setNaverProfileEmail(email);
                                mPreferences.setNaverProfileUri(profile_image);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.d(TAG, "kth getNaverProfile() failed. buffer : " + buffer.toString());
                }
            }
        }.execute();
    }

    private void initKakao(){
        setKakaoButtonText();

        mKakaoSessionCallback = new SessionCallback();
        if(Session.getCurrentSession().isClosed()) {
            Session.getCurrentSession().addCallback(mKakaoSessionCallback);
        }
    }

    private void setKakaoButtonText() {
        btn_kakao.post(new Runnable() {
            @Override
            public void run() {
                String buttonText = "";

                int loginType = mPreferences.getLoginType();
                if(loginType == Define.LOGIN_TYPE_KAKAO){
                    buttonText = getString(R.string.logout);
                }else{
                    buttonText = getString(R.string.login_kakao);
                }

                btn_kakao.setText(buttonText);
            }
        });
    }

    private void getKakaoProfile(){
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d(TAG, "kth UserManagement.requestMe() onSessionClosed()");
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d(TAG, "kth UserManagement.requestMe() onSuccess()");
                String profile_image = userProfile.getProfileImagePath();
                String email = userProfile.getEmail();
                String name = userProfile.getNickname();

                mPreferences.setKakaoProfileName(name);
                mPreferences.setKakaoProfileEmail(email);
                mPreferences.setKakaoProfileUri(profile_image);
            }

            @Override
            public void onNotSignedUp() {
                Log.d(TAG, "kth UserManagement.requestMe() onNotSignedUp()");
            }
        });
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            Log.d(TAG, "kth SessionCallback onSessionOpened()");
            mPreferences.setLoginType(Define.LOGIN_TYPE_KAKAO);
            setResult(RESULT_OK);
            setKakaoButtonText();
            getKakaoProfile();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
            }
        }
    }
}
