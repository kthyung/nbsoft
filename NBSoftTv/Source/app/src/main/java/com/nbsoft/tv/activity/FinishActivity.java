package com.nbsoft.tv.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.nbsoft.tv.AdSingleton;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;

public class FinishActivity extends AppCompatActivity {
    public static final String TAG = FinishActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private RelativeLayout rl_content;
    private TextView tv_cancel, tv_ok;

    private AdView adView;

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
                case R.id.tv_cancel:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
                case R.id.tv_ok:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setDimAmount(0.8f);
        getWindow().setGravity(Gravity.CENTER);
        setContentView(R.layout.activity_finish);

        setFinishOnTouchOutside(false);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        initAdvertisement();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(adView != null) {
            rl_content.removeView(adView);
        }

        super.onDestroy();
    }

    private void initLayout(){
        rl_content = (RelativeLayout) findViewById(R.id.rl_content);

        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_cancel.setClickable(true);
        tv_cancel.setOnClickListener(onClickListener);

        tv_ok = (TextView) findViewById(R.id.tv_ok);
        tv_ok.setClickable(true);
        tv_ok.setOnClickListener(onClickListener);
    }

    private void initAdvertisement(){
        adView = AdSingleton.getInstance(mContext).getBannerAdView();
        if(adView != null) {
            rl_content.addView(adView);
        }
    }
}
