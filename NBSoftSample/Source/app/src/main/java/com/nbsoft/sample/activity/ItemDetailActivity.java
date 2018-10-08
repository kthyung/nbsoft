package com.nbsoft.sample.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.R;

public class ItemDetailActivity extends AppCompatActivity {
    public static final String TAG = ItemDetailActivity.class.getSimpleName();

    private Context mContext;

    private int mItemId;

    private Toolbar toolbar;
    private TextView tv_toolbar_title;

    private RelativeLayout rl_toolbar_drawer;

    private ImageView iv_content;
    private TextView tv_name, tv_description;
    private Button btn_webview1, btn_webview2, btn_webview3;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_webview1:
                    showWebView("http://www.google.com");
                    break;
                case R.id.btn_webview2:
                    showWebView("http://www.opentutorials.org");
                    break;
                case R.id.btn_webview3:
                    showWebView("http://androidweekly.net/");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

        mContext = this;

        Intent intent = getIntent();
        if(intent!=null){
            if(!intent.hasExtra("itemId")){
                Log.d(TAG, "kth onCreate() itemId is empty.");
                finish();
                return;
            }

            mItemId = intent.getIntExtra("itemId", 0);
            initLayout();
        }
    }

    private void initLayout(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        // Custom Toolbar
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        View viewToolbar = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_toolbar_main, null);
        actionBar.setCustomView(viewToolbar, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        tv_toolbar_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText("Item Detail");

        rl_toolbar_drawer = (RelativeLayout) findViewById(R.id.rl_toolbar_drawer);
        rl_toolbar_drawer.setClickable(false);
        rl_toolbar_drawer.setOnClickListener(null);
        rl_toolbar_drawer.setVisibility(View.INVISIBLE);

        iv_content = (ImageView) findViewById(R.id.iv_content);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_description = (TextView) findViewById(R.id.tv_description);
        btn_webview1 = (Button) findViewById(R.id.btn_webview1);
        btn_webview2 = (Button) findViewById(R.id.btn_webview2);
        btn_webview3 = (Button) findViewById(R.id.btn_webview3);

        iv_content.setImageResource(R.mipmap.ic_launcher);

        btn_webview1.setOnClickListener(onClickListener);
        btn_webview2.setOnClickListener(onClickListener);
        btn_webview3.setOnClickListener(onClickListener);
    }

    private void showWebView(String url){
        Intent intent = new Intent(ItemDetailActivity.this, ItemWebViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("webUrl", url);
        startActivity(intent);
    }
}
