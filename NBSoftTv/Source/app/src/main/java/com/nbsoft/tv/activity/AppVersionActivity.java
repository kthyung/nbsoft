package com.nbsoft.tv.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;
import com.nbsoft.tv.etc.AppUtil;

public class AppVersionActivity extends AppCompatActivity {
    public static final String TAG = AppVersionActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private TextView tv_current, tv_recent;
    private Button btn_update, btn_opensource;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_toolbar_left:
                    finish();
                    break;
                case R.id.btn_update:
                    new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                            .setTitle(mContext.getString(R.string.popup_permission_title))
                            .setMessage(mContext.getString(R.string.popup_update_msg))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    break;
                case R.id.btn_opensource:
                    Intent intent = new Intent(AppVersionActivity.this, OssLicensesMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appversion);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        versionCheck();
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
        tv_toolbar_title.setText(mContext.getString(R.string.title_appversion));

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.btn_title_befor_nor);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(true);
        rl_toolbar_left.setOnClickListener(onClickListener);
        rl_toolbar_left.setVisibility(View.VISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.btn_title_option_nor);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(false);
        rl_toolbar_right.setOnClickListener(null);
        rl_toolbar_right.setVisibility(View.INVISIBLE);

        tv_current = (TextView) findViewById(R.id.tv_current);
        tv_recent = (TextView) findViewById(R.id.tv_recent);

        btn_update = (Button) findViewById(R.id.btn_update);
        btn_update.setEnabled(false);
        btn_update.setClickable(false);
        btn_update.setOnClickListener(null);

        btn_opensource = (Button) findViewById(R.id.btn_opensource);
        btn_opensource.setClickable(true);
        btn_opensource.setOnClickListener(onClickListener);
    }

    @SuppressLint("StringFormatInvalid")
    private void versionCheck(){
        String deviceVersion = mPreferences.getAppCurrentVersion();
        String marketVersion = mPreferences.getAppRecentVersion();

        tv_current.setText(mContext.getString(R.string.title_appversion_current, deviceVersion));
        tv_recent.setText(mContext.getString(R.string.title_appversion_recent, marketVersion));

        int update = AppUtil.checkVersion(deviceVersion, marketVersion);
        if(update == 1 || update == 2){
            btn_update.setEnabled(true);
            btn_update.setClickable(true);
            btn_update.setOnClickListener(onClickListener);
        }else{
            btn_update.setEnabled(false);
            btn_update.setClickable(false);
            btn_update.setOnClickListener(null);
        }
    }


}
