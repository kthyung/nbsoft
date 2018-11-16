package com.nbsoft.tv.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelBrandingSettings;
import com.google.api.services.youtube.model.ChannelSettings;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.ImageSettings;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.GlideApp;
import com.nbsoft.tv.R;
import com.nbsoft.tv.etc.LinkifyUtil;
import com.nbsoft.tv.etc.StringUtil;
import com.nbsoft.tv.model.FirebaseDataItem;
import com.nbsoft.tv.model.YoutuberBookmark;
import com.nbsoft.tv.youtube.YoutubeGetChannelInfo;
import com.nbsoft.tv.youtube.YoutubeGetPlaylist;
import com.nbsoft.tv.youtube.YoutubeGetPlaylistItems;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChannelInfoActivity extends AppCompatActivity {
    public static final String TAG = ChannelInfoActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private String mCid = "";
    private String mPageToken = "";

    private Channel mCurrentChannel;

    private YoutuberBookmark mBookMark = new YoutuberBookmark();
    private HashMap<String, FirebaseDataItem> mDataHashMap = new HashMap<String, FirebaseDataItem>();

    private ImageView iv_large, iv_round, iv_channel, iv_bookmark;
    private TextView tv_name, tv_desc, tv_keyword, tv_video, tv_subscriber, tv_view;
    private RelativeLayout rl_bookmark, rl_channel;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_round:
                    showGoChannelDialog();
                    break;
                case R.id.rl_bookmark:
                    /*ChannelBrandingSettings channelBrandingSettings = mCurrentChannel.getBrandingSettings();
                    ChannelSettings channelSettings = channelBrandingSettings.getChannel();

                    if(mDataHashMap.containsKey(mCurrentChannel.getId())){
                        Log.d(TAG, "kth onClickListener onClick() mDataHashMap.remove() itemList.getCid() : " + mCurrentChannel.getId());
                        mDataHashMap.remove(mCurrentChannel.getId());
                    }else{
                        Log.d(TAG, "kth onClickListener onClick() mDataHashMap.put() itemList.getCid() : " + mCurrentChannel.getId());
                        mDataHashMap.put(mCurrentChannel.getId(), mCurrentChannel);
                    }

                    mBookMark.setDataMap(mDataHashMap);
                    mPreferences.setYoutuberBookmark(new Gson().toJson(mBookMark));

                    if(mDataHashMap.containsKey(mCurrentChannel.getId())){
                        iv_bookmark.setImageResource(android.R.drawable.btn_star_big_on);
                        Toast.makeText(mContext, mContext.getString(R.string.youtuber_bookmark_add, channelSettings.getTitle()), Toast.LENGTH_SHORT).show();
                    }else{
                        iv_bookmark.setImageResource(android.R.drawable.btn_star_big_off);
                        Toast.makeText(mContext, mContext.getString(R.string.youtuber_bookmark_remove, channelSettings.getTitle()), Toast.LENGTH_SHORT).show();
                    }*/
                    break;
                case R.id.rl_channel:
                    showGoChannelDialog();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setDimAmount(0.8f);
        setContentView(R.layout.activity_channelinfo);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra("cid")){
                mCid = intent.getStringExtra("cid");
            }
        }

        initLayout();
        loadData();
    }

    private void initLayout(){
        iv_large = (ImageView) findViewById(R.id.iv_large);
        iv_round = (ImageView) findViewById(R.id.iv_round);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_desc = (TextView) findViewById(R.id.tv_desc);
        tv_keyword = (TextView) findViewById(R.id.tv_keyword);
        tv_video = (TextView) findViewById(R.id.tv_video);
        tv_subscriber = (TextView) findViewById(R.id.tv_subscriber);
        tv_view = (TextView) findViewById(R.id.tv_view);
        rl_bookmark = (RelativeLayout) findViewById(R.id.rl_bookmark);
        rl_channel = (RelativeLayout) findViewById(R.id.rl_channel);
        iv_channel = (ImageView) findViewById(R.id.iv_channel);
        iv_bookmark = (ImageView) findViewById(R.id.iv_bookmark);

        iv_round.setOnClickListener(onClickListener);
        rl_bookmark.setOnClickListener(onClickListener);
        rl_channel.setOnClickListener(onClickListener);
    }

    private void loadData(){
        String youtuberBookmark = mPreferences.getYoutuberBookmark();
        if(!TextUtils.isEmpty(youtuberBookmark)){
            mBookMark = new Gson().fromJson(youtuberBookmark, YoutuberBookmark.class);
            if(mBookMark!=null){
                mDataHashMap = mBookMark.getDataMap();
            }
        }

        YoutubeGetChannelInfo getChannelInfo = new YoutubeGetChannelInfo(mContext);
        getChannelInfo.getYoutubeChannelInfo(mCid, mPageToken, new YoutubeGetChannelInfo.YoutubeGetChannelInfoListener() {
            @Override
            public void onSuccess(List<Channel> resultList, String pageToken) {
                Log.d(TAG, "kth loadData() getYoutubeChannelInfo() onSuccess() resultList : " + resultList);
                Log.d(TAG, "kth loadData() getYoutubeChannelInfo() onSuccess() pageToken : " + pageToken);
                ChannelInfoActivity.this.mPageToken = pageToken;
                if(resultList!=null && !resultList.isEmpty()) {
                    mCurrentChannel = resultList.get(0);
                }

                refreshListView();
            }

            @Override
            public void onFail(Exception e) {
                Log.d(TAG, "kth loadData() getYoutubeChannelInfo() onFail() message : " + (e!=null ? e.getMessage() : ""));
            }

            @Override
            public void onAuthFail(Exception e) {
                Log.d(TAG, "kth loadData() getYoutubeChannelInfo() onAuthFail()");
                //startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);
            }
        });
    }

    private void refreshListView(){
        if(mCurrentChannel == null){
            return;
        }

        ChannelBrandingSettings channelBrandingSettings = mCurrentChannel.getBrandingSettings();
        ChannelSnippet channelSnippet = mCurrentChannel.getSnippet();
        ChannelStatistics channelStatistics = mCurrentChannel.getStatistics();
        ChannelSettings channelSettings = channelBrandingSettings.getChannel();

        ImageSettings imageSettings = channelBrandingSettings.getImage();
        if(imageSettings != null){
            String imageUrl = imageSettings.getBannerMobileHdImageUrl();
            if(TextUtils.isEmpty(imageUrl)){
                imageUrl = imageSettings.getBannerMobileMediumHdImageUrl();
            }
            if(TextUtils.isEmpty(imageUrl)){
                imageUrl = imageSettings.getBannerMobileLowImageUrl();
            }
            if(!TextUtils.isEmpty(imageUrl)){
                GlideApp.with(mContext)
                        .load(imageUrl)
                        .into(iv_large);
            }
        }

        ThumbnailDetails thumbnailDetails = channelSnippet.getThumbnails();
        if(thumbnailDetails != null){
            Thumbnail thumbnail = thumbnailDetails.getHigh();
            if(thumbnail == null){
                thumbnail = thumbnailDetails.getMedium();
            }
            if(thumbnail == null){
                thumbnail = thumbnailDetails.getStandard();
            }
            if(thumbnail != null){
                GlideApp.with(mContext)
                        .load(thumbnail.getUrl())
                        .apply(new RequestOptions().circleCrop())
                        .into(iv_round);
            }
        }

        if(channelSettings != null){
            tv_name.setText(channelSettings.getTitle());

            String description = channelSettings.getDescription();
            if(TextUtils.isEmpty(description)){
                tv_desc.setText("");
                tv_desc.setVisibility(View.INVISIBLE);
            }else{
                tv_desc.setText(description);
                tv_desc.setVisibility(View.VISIBLE);

                LinkifyUtil.addLinks(tv_desc, LinkifyUtil.ALL);
            }

            String keyword = channelSettings.getKeywords();
            if(TextUtils.isEmpty(keyword)){
                tv_keyword.setText("");
                tv_keyword.setVisibility(View.GONE);
            }else{
                tv_keyword.setText(mContext.getString(R.string.youtuber_channelinfo_keyword, keyword));
                tv_keyword.setVisibility(View.VISIBLE);
            }
        }

        if(channelStatistics != null){
            BigInteger videoCount = channelStatistics.getVideoCount();
            if(videoCount != null){
                tv_video.setText(mContext.getString(R.string.youtuber_channelinfo_video, StringUtil.getFormatedNumber(videoCount.toString())));
            }

            BigInteger viewCount = channelStatistics.getViewCount();
            if(viewCount != null){
                tv_view.setText(mContext.getString(R.string.youtuber_channelinfo_view, StringUtil.getFormatedNumber(viewCount.toString())));
            }

            BigInteger subscriberCount = channelStatistics.getSubscriberCount();
            if(subscriberCount != null){
                tv_subscriber.setText(mContext.getString(R.string.youtuber_channelinfo_subscriber, StringUtil.getFormatedNumber(subscriberCount.toString())));
            }
        }

        if(mDataHashMap.containsKey(mCurrentChannel.getId())){
            iv_bookmark.setImageResource(android.R.drawable.btn_star_big_on);
        }else{
            iv_bookmark.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void showGoChannelDialog(){
        new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle(mContext.getString(R.string.popup_youtube_channel_title))
                .setMessage(mContext.getString(R.string.popup_youtube_channel_msg))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://www.youtube.com/channel/" + mCurrentChannel.getId()));
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
    }
}
