package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.api.services.youtube.model.Playlist;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;
import com.nbsoft.tv.model.FirebaseDataItem;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = SettingsActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private RelativeLayout rl_request, rl_bookmark, rl_3glte, rl_autoplay;
    private RelativeLayout rl_notice, rl_showad, rl_appinfo, rl_appversion;
    private SwitchCompat sc_3glte, sc_autoplay;

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
                case R.id.rl_toolbar_left:
                    finish();
                    break;
                case R.id.rl_request: {
                    Intent intent = new Intent(SettingsActivity.this, YoutuberRequestActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;
                case R.id.rl_bookmark:{
                    Intent intent = new Intent(SettingsActivity.this, YoutuberBookmarkActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;
                /*case R.id.rl_history:{
                    Intent intent = new Intent(SettingsActivity.this, YoutuberHistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;*/
                case R.id.rl_3glte:{
                    sc_3glte.performClick();
                }
                    break;
                case R.id.rl_autoplay:{
                    sc_autoplay.performClick();
                }
                    break;
                /*case R.id.rl_set_history:{
                    mPreferences.setSaveHistory(!mPreferences.getSaveHistory());
                }
                    break;*/
                case R.id.rl_notice:{
                    Intent intent = new Intent(SettingsActivity.this, NoticeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;
                case R.id.rl_showad:{
                    Intent intent = new Intent(SettingsActivity.this, ShowAdActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;
                case R.id.rl_appinfo:{
                    Intent intent = new Intent(SettingsActivity.this, AppInfoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;
                case R.id.rl_appversion:{
                    Intent intent = new Intent(SettingsActivity.this, AppVersionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                    break;
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.sc_3glte:
                    mPreferences.set3gLteAccept(isChecked);
                    break;
                case R.id.sc_autoplay:
                    mPreferences.setAutoPlay(isChecked);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        initSetting();
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
        tv_toolbar_title.setText(mContext.getString(R.string.settings_title));

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
    }

    private void initSetting(){
        rl_request = (RelativeLayout) findViewById(R.id.rl_request);
        rl_bookmark = (RelativeLayout) findViewById(R.id.rl_bookmark);
        rl_3glte = (RelativeLayout) findViewById(R.id.rl_3glte);
        rl_autoplay = (RelativeLayout) findViewById(R.id.rl_autoplay);
        rl_notice = (RelativeLayout) findViewById(R.id.rl_notice);
        rl_showad = (RelativeLayout) findViewById(R.id.rl_showad);
        rl_appinfo = (RelativeLayout) findViewById(R.id.rl_appinfo);
        rl_appversion = (RelativeLayout) findViewById(R.id.rl_appversion);

        rl_request.setClickable(true);
        rl_request.setOnClickListener(onClickListener);

        rl_bookmark.setClickable(true);
        rl_bookmark.setOnClickListener(onClickListener);

        rl_3glte.setClickable(true);
        rl_3glte.setOnClickListener(onClickListener);

        sc_3glte = (SwitchCompat) findViewById(R.id.sc_3glte);
        sc_3glte.setChecked(mPreferences.get3gLteAccept());
        sc_3glte.setOnCheckedChangeListener(onCheckedChangeListener);

        rl_autoplay.setClickable(true);
        rl_autoplay.setOnClickListener(onClickListener);

        sc_autoplay = (SwitchCompat) findViewById(R.id.sc_autoplay);
        sc_autoplay.setChecked(mPreferences.getAutoPlay());
        sc_autoplay.setOnCheckedChangeListener(onCheckedChangeListener);

        rl_notice.setClickable(true);
        rl_notice.setOnClickListener(onClickListener);

        rl_showad.setClickable(true);
        rl_showad.setOnClickListener(onClickListener);

        rl_appinfo.setClickable(true);
        rl_appinfo.setOnClickListener(onClickListener);

        rl_appversion.setClickable(true);
        rl_appversion.setOnClickListener(onClickListener);
    }
}
