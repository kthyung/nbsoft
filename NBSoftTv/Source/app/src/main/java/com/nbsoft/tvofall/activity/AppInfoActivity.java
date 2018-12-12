package com.nbsoft.tvofall.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.R;
import com.nbsoft.tvofall.activity.fragment.AppInfoDescFragment;
import com.nbsoft.tvofall.activity.fragment.AppInfoRecommandFragment;
import com.nbsoft.tvofall.activity.fragment.AppInfoSendFragment;

import java.util.ArrayList;
import java.util.List;

public class AppInfoActivity extends AppCompatActivity {
    public static final String TAG = AppInfoActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private AppInfoDescFragment mAppInfoDescFragment;
    private AppInfoSendFragment mAppInfoSendFragment;
    private AppInfoRecommandFragment mAppInfoRecommandFragment;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private TabLayout tabs;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

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
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        initViewPager();
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
        tv_toolbar_title.setText(mContext.getString(R.string.title_appinfo));

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

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.vp_content);
        viewPager.setOffscreenPageLimit(3);

        mAppInfoDescFragment = new AppInfoDescFragment();
        mAppInfoSendFragment = new AppInfoSendFragment();
        mAppInfoRecommandFragment = new AppInfoRecommandFragment();

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mAppInfoDescFragment, mContext.getString(R.string.title_appinfo_desc));
        adapter.addFragment(mAppInfoSendFragment, mContext.getString(R.string.title_appinfo_send));
        adapter.addFragment(mAppInfoRecommandFragment, mContext.getString(R.string.title_appinfo_recommand));

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mPreferences.setLastYoutuberType(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        tabs = (TabLayout) findViewById(R.id.tl_content);
        tabs.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(0);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<Fragment>();
        private final List<String> mFragmentTitleList = new ArrayList<String>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
