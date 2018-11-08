package com.nbsoft.sample.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.R;
import com.nbsoft.sample.activity.fragment.YoutuberBookmarkFragment;
import com.nbsoft.sample.activity.fragment.YoutuberTotalFragment;

import java.util.ArrayList;
import java.util.List;

public class YoutuberActivity extends AppCompatActivity {
    public static final String TAG = YoutuberActivity.class.getSimpleName();

    private Context mContext;

    private Toolbar toolbar;
    private TextView tv_toolbar_title;

    private ImageView iv_toolbar_drawer;
    private RelativeLayout rl_toolbar_drawer;
    private RelativeLayout rl_toolbar_info;

    private TabLayout tabs;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private TabLayout.Tab tabnote, tabletter;
    private TextView tv_tabtitleNote, tv_tabtitleLetter;

    public int mCurrentParentTabPosition = 0;

    private YoutuberTotalFragment totalFragment;
    private YoutuberBookmarkFragment bookmarkFragment;

    private AppPreferences mPreferences;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_toolbar_drawer:
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber);

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

        mContext = this;

        mPreferences = new AppPreferences(mContext);

        initLayout();
        initViewPager();
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
        tv_toolbar_title.setText("Youtuber");

        iv_toolbar_drawer = (ImageView) findViewById(R.id.iv_toolbar_drawer);
        iv_toolbar_drawer.setImageResource(R.drawable.btn_title_befor_nor);

        rl_toolbar_drawer = (RelativeLayout) findViewById(R.id.rl_toolbar_drawer);
        rl_toolbar_drawer.setClickable(true);
        rl_toolbar_drawer.setOnClickListener(onClickListener);

        rl_toolbar_info = (RelativeLayout) findViewById(R.id.rl_toolbar_info);
        rl_toolbar_info.setClickable(false);
        rl_toolbar_info.setOnClickListener(null);
        rl_toolbar_info.setVisibility(View.INVISIBLE);
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.vp_content);
        viewPager.setOffscreenPageLimit( 2 );
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "kth initViewPager() onPageSelected() position : " + position);
                mCurrentParentTabPosition = position;

                if(position == 0) {
                    tv_tabtitleNote.setTextColor(getResources().getColor(R.color.color_e191f7));  //select
                    tv_tabtitleLetter.setTextColor(getResources().getColor(R.color.color_dddddd));  //unselect
                }else if(position == 1) {
                    tv_tabtitleNote.setTextColor(getResources().getColor(R.color.color_dddddd));  //select
                    tv_tabtitleLetter.setTextColor(getResources().getColor(R.color.color_e191f7));  //unselect
                }
            }
        });

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        totalFragment = new YoutuberTotalFragment();
        bookmarkFragment = new YoutuberBookmarkFragment();
        adapter.addFragment(totalFragment, mContext.getString(R.string.youtuber_tab_1));
        adapter.addFragment(bookmarkFragment, mContext.getString(R.string.youtuber_tab_2));
        viewPager.setAdapter(adapter);

        // Set Tabs inside Toolbar
        tabs = (TabLayout) findViewById(R.id.tl_content);
        tabs.setupWithViewPager(viewPager);

        tabnote = tabs.getTabAt(0);
        tabnote.setCustomView(R.layout.layout_tab_item);

        tv_tabtitleNote = tabnote.getCustomView().findViewById(R.id.tv_tabtitle);
        tv_tabtitleNote.setText(mContext.getString(R.string.youtuber_tab_1));

        tabletter = tabs.getTabAt(1);
        tabletter.setCustomView(R.layout.layout_tab_item);

        tv_tabtitleLetter = tabletter.getCustomView().findViewById(R.id.tv_tabtitle);
        tv_tabtitleLetter.setText(mContext.getString(R.string.youtuber_tab_2));

        tv_tabtitleNote.setTextColor(getResources().getColor(R.color.color_e191f7));  //select
        tv_tabtitleLetter.setTextColor(getResources().getColor(R.color.color_dddddd));  //unselect
    }

    public void refreshFragment(int position){
        if(position == 0){
            totalFragment.refreshListView();
        }else if(position == 1){
            bookmarkFragment.refreshListView();
        }
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

        public String getTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
            // return null to display only the icon
            //return null;
        }
    }
}
