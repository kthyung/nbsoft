package com.nbsoft.tvofall.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nbsoft.tvofall.AdSingleton;
import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.GlobalSingleton;
import com.nbsoft.tvofall.R;
import com.nbsoft.tvofall.activity.fragment.YoutuberFragment;
import com.nbsoft.tvofall.etc.AppUtil;
import com.nbsoft.tvofall.etc.NetworkUtil;
import com.nbsoft.tvofall.model.FirebaseDataItem;
import com.nbsoft.tvofall.model.FirebaseItem;
import com.nbsoft.tvofall.model.YoutuberBookmark;
import com.nbsoft.tvofall.view.LoadingPopupManager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class YoutuberActivity extends AppCompatActivity {
    public static final String TAG = YoutuberActivity.class.getSimpleName();

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_FINISH = 1003;

    public static final int REQUEST_CODE_PERMISSION = 10000;

    private Context mContext;
    private DatabaseReference mDatabase;
    private GoogleAccountCredential mCredential;
    private AppPreferences mPreferences;

    private List<FirebaseDataItem> mArrDataList;

    private YoutuberBookmark mBookMark = new YoutuberBookmark();
    private HashMap<String, FirebaseDataItem> mBookMarkHashMap = new HashMap<String, FirebaseDataItem>();

    private YoutuberFragment[] mYoutuberFragment;
    private String[] mArrYoutuberType;

    private long backKeyPressedTime = 0L;
    private Toast mToastBackPressed = null;

    private InterstitialAd mInterstitialAd;

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
                case R.id.rl_toolbar_right:
                    showMenu(v);
                    break;
            }
        }
    };

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            Log.d(TAG, "kth ValueEventListener onDataChange() DataSnapshot : " + (dataSnapshot!=null ? dataSnapshot.toString() : "null"));
            try{
                FirebaseItem obj = dataSnapshot.getValue(FirebaseItem.class);
                if(obj != null){
                    mArrDataList = obj.getData();
                    Collections.sort(mArrDataList, new Comparator<FirebaseDataItem>() {
                        private final Collator collator = Collator.getInstance(new Locale("ko"));

                        @Override
                        public int compare(FirebaseDataItem o1, FirebaseDataItem o2) {
                            boolean isLeftNumber = true;
                            boolean isRightNumber = true;

                            char leftChar = o1.getName().charAt(0);
                            char rightChar = o2.getName().charAt(0);
                            if(leftChar < '0' || leftChar > '9'){
                                isLeftNumber = false;
                            }

                            if(rightChar < '0' || rightChar > '9'){
                                isRightNumber = false;
                            }

                            if(isLeftNumber && !isRightNumber) {
                                return 1;
                            } else if(!isLeftNumber && isRightNumber) {
                                return -1;
                            } else {
                                return collator.compare(o1.getName(), o2.getName());
                            }
                        }
                    });

                    HashMap<String, FirebaseDataItem> dataList = new HashMap<String, FirebaseDataItem>();
                    for(FirebaseDataItem item : mArrDataList){
                        dataList.put(item.getCid(), item);
                    }
                    GlobalSingleton.getInstance().setDataList(dataList);
                    GlobalSingleton.getInstance().setNoticeList(obj.getNotice());

                    initViewPager();
                    checkTutorial();
                    checkVersion();

                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberActivity");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.d(TAG, "kth ValueEventListener onCancelled() DatabaseError : ", databaseError.toException());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber);

        mContext = this;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mPreferences = new AppPreferences(mContext);

        initLayout();

        if((ContextCompat.checkSelfPermission(mContext, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)){
            new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle(mContext.getString(R.string.popup_permission_title))
                    .setMessage(mContext.getString(R.string.popup_permission_msg))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(YoutuberActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_CODE_PERMISSION);
                        }
                    })
                    .show();
        }else{
            if(NetworkUtil.networkStateCheck(mContext) == NetworkUtil.NETWORK_DISCONNECTED){
                new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                        .setTitle(mContext.getString(R.string.popup_permission_title))
                        .setMessage(mContext.getString(R.string.error_network))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();

                return;
            }

            initAdvertisement();
            initGoogleAccount();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(YoutuberActivity.this, FinishActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_FINISH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0) {
                    boolean isAllOk = true;
                    for (int item : grantResults) {
                        if (item == -1) {
                            isAllOk = false;
                            break;
                        }
                    }

                    if(NetworkUtil.networkStateCheck(mContext) == NetworkUtil.NETWORK_DISCONNECTED){
                        new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                                .setTitle(mContext.getString(R.string.popup_permission_title))
                                .setMessage(mContext.getString(R.string.error_network))
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();

                        return;
                    }

                    initAdvertisement();
                    initGoogleAccount();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "kth onActivityResult() REQUEST_GOOGLE_PLAY_SERVICES resultCode != RESULT_OK");
                } else {
                    loadData();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    Log.d(TAG, "kth onActivityResult() REQUEST_ACCOUNT_PICKER resultCode == RESULT_OK");
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mPreferences.setGoogleAccountName(accountName);
                        mCredential.setSelectedAccountName(accountName);
                        loadData();
                    }
                }
                break;
            case REQUEST_FINISH:
                if (resultCode == Activity.RESULT_OK){
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        GlobalSingleton.getInstance().removeGlobalInstances();
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
        tv_toolbar_title.setText(mContext.getString(R.string.app_name));

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.outline_arrow_back_ios_white_48);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(false);
        rl_toolbar_left.setOnClickListener(null);
        rl_toolbar_left.setVisibility(View.INVISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.outline_more_vert_white_48);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(true);
        rl_toolbar_right.setOnClickListener(onClickListener);
        rl_toolbar_right.setVisibility(View.VISIBLE);
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.vp_content);
        viewPager.setOffscreenPageLimit(mArrYoutuberType.length);

        mYoutuberFragment = new YoutuberFragment[mArrYoutuberType.length];

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        for(int i=0; i<mArrYoutuberType.length; i++){
            mYoutuberFragment[i] = new YoutuberFragment();

            ArrayList<FirebaseDataItem> tempArr = new ArrayList<FirebaseDataItem>();
            for(FirebaseDataItem item : mArrDataList){
                String typeStr = AppUtil.getTypeToString(i);
                if(item.getType().equals(typeStr)){
                    tempArr.add(item);
                }
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("data", tempArr);

            mYoutuberFragment[i].setArguments(bundle);
            adapter.addFragment(mYoutuberFragment[i], mArrYoutuberType[i]);
        }

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

        viewPager.setCurrentItem(mPreferences.getLastYoutuberType());
    }

    public void initAdvertisement(){
        AdSingleton.getInstance(mContext).initAdvertisement();
        AdSingleton.getInstance(mContext).initBannerAdView();
        AdSingleton.getInstance(mContext).initRewardedVideo();
    }

    public void initGoogleAccount(){
        mCredential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(new String[]{YouTubeScopes.YOUTUBE_READONLY}))
                .setBackOff(new ExponentialBackOff());

        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            String googleAccountName = mPreferences.getGoogleAccountName();
            if(TextUtils.isEmpty(googleAccountName)){
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }else{
                mCredential.setSelectedAccountName(googleAccountName);
                loadData();
            }
        } else {
            loadData();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(YoutuberActivity.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void loadData(){
        LoadingPopupManager.getInstance(mContext).showLoading(YoutuberActivity.this, true, "YoutuberActivity");

        mArrYoutuberType = mContext.getResources().getStringArray(R.array.arr_youtuber_type);
        mDatabase.addListenerForSingleValueEvent(valueEventListener);

        String youtuberBookmark = mPreferences.getYoutuberBookmark();
        if(!TextUtils.isEmpty(youtuberBookmark)){
            mBookMark = new Gson().fromJson(youtuberBookmark, YoutuberBookmark.class);
            if(mBookMark!=null){
                mBookMarkHashMap = mBookMark.getDataMap();
            }
        }
    }

    public void refreshFragment(int position){
        if(position < mYoutuberFragment.length){
            if(mYoutuberFragment[position] != null){
                mYoutuberFragment[position].refreshListView();
            }
        }
    }

    public void showChannelInfo(FirebaseDataItem item){
        if(item == null){
            return;
        }

        Log.d(TAG, "kth showChannelInfo() item.getCid() : " + item.getCid());
        Intent intent = new Intent(YoutuberActivity.this, ChannelInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("cid", item.getCid());
        startActivity(intent);
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_request: {
                        Intent intent = new Intent(YoutuberActivity.this, YoutuberRequestActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    return true;
                    case R.id.menu_bookmark: {
                        Intent intent = new Intent(YoutuberActivity.this, YoutuberBookmarkActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    return true;
                    /*case R.id.menu_history: {
                        Intent intent = new Intent(YoutuberActivity.this, YoutuberHistoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    return true;*/
                    case R.id.menu_setting: {
                        Intent intent = new Intent(YoutuberActivity.this, SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    return true;
                }

                return false;
            }
        });
        popup.inflate(R.menu.menu_youtuber);
        popup.show();
    }

    private void checkTutorial(){
        if(mPreferences.getShowTutorial()){
            /*Log.d(TAG, "kth checkTutorial()");
            Intent intent = new Intent(YoutuberActivity.this, TutorialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
        }
    }

    private void checkVersion(){
        new VersionCheckProcessTask().execute();
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

    public class VersionCheckProcessTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            String deviceVersion = AppUtil.getApplicationVersion(mContext);
            String marketVersion = AppUtil.getMarketVersion(mContext);
            if(TextUtils.isEmpty(marketVersion)){
                marketVersion = deviceVersion;
            }

            if(!mPreferences.getAppRecentVersion().equals(marketVersion)){
                mPreferences.setShowUpdatePopup(false);
            }

            mPreferences.setAppCurrentVersion(deviceVersion);
            mPreferences.setAppRecentVersion(marketVersion);

            return AppUtil.checkVersion(deviceVersion, marketVersion);
        }

        @Override
        protected void onPostExecute(Integer update) {
            if(update == 1){
                //메이저 업데이트 팝업 노출
                new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                        .setTitle(mContext.getString(R.string.popup_permission_title))
                        .setMessage(mContext.getString(R.string.popup_update_intro_msg))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                                try{
                                    intent.setData(Uri.parse("market://details?id=" + mContext.getPackageName()));
                                    startActivity(intent);
                                }catch(Exception e){
                                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                                    startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mPreferences.setShowUpdatePopup(true);
                            }
                        })
                        .show();
            }else if(update == 2){
                if(!mPreferences.getShowUpdatePopup()) {
                    //마이너 업데이트 팝업 노출
                    new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                            .setTitle(mContext.getString(R.string.popup_permission_title))
                            .setMessage(mContext.getString(R.string.popup_update_intro_msg))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                                    try {
                                        intent.setData(Uri.parse("market://details?id=" + mContext.getPackageName()));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    mPreferences.setShowUpdatePopup(true);
                                }
                            })
                            .show();
                }
            }

            super.onPostExecute(update);
        }
    }
}
