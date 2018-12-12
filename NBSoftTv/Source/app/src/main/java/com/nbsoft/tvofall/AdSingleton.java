package com.nbsoft.tvofall;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class AdSingleton {
    private static final String TAG = AdSingleton.class.getSimpleName();

    private volatile static AdSingleton uniqueInstance;

    private Context mContext;

    private AdView mBannerAdView;
    private RewardedVideoAd mRewardedVideoAd;

    private AdSingleton(Context context) {
        mContext = context;
    }

    public static AdSingleton getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized(AdSingleton.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new AdSingleton(context);
                }
            }
        }

        return uniqueInstance;
    }

    public void initAdvertisement(){
        MobileAds.initialize(mContext, mContext.getString(R.string.key_ad_appid));
        //MobileAds.initialize(mContext, "ca-app-pub-3940256099942544~3347511713");
    }

    public void initBannerAdView(){
        mBannerAdView = new AdView(mContext);
        mBannerAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        mBannerAdView.setAdUnitId(mContext.getString(R.string.key_ad_unitid_banner));
        //mBannerAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        mBannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "kth initAdvertisement() onAdLoaded()");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "kth initAdvertisement() onAdFailedToLoad() errorCode : " + errorCode);
            }

            @Override
            public void onAdOpened() {
                Log.d(TAG, "kth initAdvertisement() onAdOpened()");
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "kth initAdvertisement() onAdLeftApplication()");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "kth initAdvertisement() onAdClosed()");
                if(mBannerAdView != null){
                    mBannerAdView.loadAd(new AdRequest.Builder().build());
                }
            }
        });

        mBannerAdView.loadAd(new AdRequest.Builder().build());
    }

    public AdView getBannerAdView() {
        return mBannerAdView;
    }

    public void setBannerAdView(AdView mAdView) {
        this.mBannerAdView = mAdView;
    }

    public void initRewardedVideo(){
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoAdLoaded()");
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoAdOpened()");
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoStarted()");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoAdClosed()");
                mRewardedVideoAd.loadAd(mContext.getString(R.string.key_ad_unitid_rewarded), new AdRequest.Builder().build());
                //mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.d(TAG, "kth initAdvertisement() onRewarded() rewardItem : " + rewardItem);

                AppPreferences mPreferences = new AppPreferences(mContext);
                int count = mPreferences.getShowAdCount()+1;
                mPreferences.setShowAdCount(count);
                mPreferences.setShowAdTime(System.currentTimeMillis());

                Toast.makeText(mContext, mContext.getString(R.string.showad_complete), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoAdLeftApplication()");
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoAdFailedToLoad()");
            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.d(TAG, "kth initAdvertisement() onRewardedVideoCompleted()");
            }
        });

        mRewardedVideoAd.loadAd(mContext.getString(R.string.key_ad_unitid_rewarded), new AdRequest.Builder().build());
        //mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }

    public RewardedVideoAd getRewardedVideoAd() {
        return mRewardedVideoAd;
    }

    public void setRewardedVideoAd(RewardedVideoAd mRewardedVideoAd) {
        this.mRewardedVideoAd = mRewardedVideoAd;
    }
}
