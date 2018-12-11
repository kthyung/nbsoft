package com.nbsoft.tv.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nbsoft.tv.AdSingleton;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;

import java.util.Calendar;

public class ShowAdActivity extends AppCompatActivity {
    public static final String TAG = ShowAdActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private RewardedVideoAd mRewardedVideoAd;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private TextView tv_text, tv_count;
    private Button btn_ok;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        private long timeStamp = 0;

        @Override
        public void onClick(View v) {
            long curTimeStamp = System.currentTimeMillis();
            if (curTimeStamp - timeStamp < 500) {
                return;
            }
            timeStamp = curTimeStamp;

            switch (v.getId()){
                case R.id.rl_toolbar_left:{
                    finish();
                    break;
                }
                case R.id.btn_ok: {
                    requestShowAd();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showad);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        initAdvertisement();
    }

    @Override
    protected void onResume() {
        if(mRewardedVideoAd != null) {
            mRewardedVideoAd.resume(mContext);
        }

        tv_count.setText(mContext.getString(R.string.showad_count, mPreferences.getShowAdCount()));

        super.onResume();
    }

    @Override
    protected void onPause() {
        if(mRewardedVideoAd != null) {
            mRewardedVideoAd.pause(mContext);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initLayout(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        // Custom Toolbar
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        View viewToolbar = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_toolbar_main, null);
        actionBar.setCustomView(viewToolbar, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        TextView tv_toolbar_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(mContext.getString(R.string.title_showad));

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.outline_arrow_back_ios_white_48);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(true);
        rl_toolbar_left.setOnClickListener(onClickListener);
        rl_toolbar_left.setVisibility(View.VISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.outline_more_vert_white_48);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(false);
        rl_toolbar_right.setOnClickListener(null);
        rl_toolbar_right.setVisibility(View.INVISIBLE);

        tv_text = (TextView) findViewById(R.id.tv_text);
        tv_text.setText(R.string.showad_text);

        int showAdCount = mPreferences.getShowAdCount();
        long showAdTime = mPreferences.getShowAdTime();

        Calendar showAdCalendar = Calendar.getInstance();
        showAdCalendar.setTimeInMillis(showAdTime);
        int showAdDay = showAdCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar nowCalendar = Calendar.getInstance();
        int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
        if(showAdDay != nowDay){
            //0으로 초기화
            showAdCount = 0;
            mPreferences.setShowAdCount(0);
        }

        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_count.setText(mContext.getString(R.string.showad_count, showAdCount));

        btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_ok.setClickable(true);
        btn_ok.setOnClickListener(onClickListener);
        btn_ok.setEnabled(true);
    }

    private void initAdvertisement(){
        mRewardedVideoAd = AdSingleton.getInstance(mContext).getRewardedVideoAd();
    }

    private void requestShowAd(){
        int showAdCount = mPreferences.getShowAdCount();
        if(showAdCount >= 5){
            Toast.makeText(mContext, mContext.getString(R.string.showad_error_count), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mRewardedVideoAd!=null && mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }
}
