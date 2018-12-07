package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelBrandingSettings;
import com.google.api.services.youtube.model.ChannelSettings;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.ContentRating;
import com.google.api.services.youtube.model.ImageSettings;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.gson.Gson;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.GlideApp;
import com.nbsoft.tv.GlobalSingleton;
import com.nbsoft.tv.R;
import com.nbsoft.tv.etc.LinkifyUtil;
import com.nbsoft.tv.etc.StringUtil;
import com.nbsoft.tv.model.FirebaseDataItem;
import com.nbsoft.tv.model.YoutuberBookmark;
import com.nbsoft.tv.view.LoadingPopupManager;
import com.nbsoft.tv.youtube.YoutubeGetChannelInfo;
import com.nbsoft.tv.youtube.YoutubeGetVideoInfo;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class VideoInfoActivity extends AppCompatActivity {
    public static final String TAG = VideoInfoActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private String mVid = "";
    private String mPageToken = "";

    private Video mCurrentVideo;

    private ImageView iv_large;
    private TextView tv_title, tv_date, tv_duration, tv_rating, tv_desc, tv_tags, tv_view, tv_like, tv_dislike;
    private ImageView iv_share, iv_video;
    private RelativeLayout rl_share, rl_video;

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
                case R.id.rl_share:
                    showShareVideoDialog();
                    break;
                case R.id.rl_video:
                    showGoVideoDialog();
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
        setContentView(R.layout.activity_videoinfo);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra("vid")){
                mVid = intent.getStringExtra("vid");
            }
        }

        initLayout();
        loadData();
    }

    private void initLayout(){
        iv_large = (ImageView)findViewById(R.id.iv_large);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
        tv_rating = (TextView) findViewById(R.id.tv_rating);
        tv_desc = (TextView) findViewById(R.id.tv_desc);
        tv_tags = (TextView) findViewById(R.id.tv_tags);
        tv_view = (TextView) findViewById(R.id.tv_view);
        tv_like = (TextView) findViewById(R.id.tv_like);
        tv_dislike = (TextView) findViewById(R.id.tv_dislike);

        rl_share = (RelativeLayout) findViewById(R.id.rl_share);
        rl_video = (RelativeLayout) findViewById(R.id.rl_video);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_video = (ImageView) findViewById(R.id.iv_video);

        rl_share.setOnClickListener(onClickListener);
        rl_video.setOnClickListener(onClickListener);
    }

    private void loadData(){
        Video video = GlobalSingleton.getInstance().getVideoListItem(mVid);
        if(video != null){
            mCurrentVideo = video;
            refreshListView();
        }else{
            YoutubeGetVideoInfo getVideoInfo = new YoutubeGetVideoInfo(mContext);
            getVideoInfo.getYoutubeVideoInfo(mVid, mPageToken, new YoutubeGetVideoInfo.YoutubeGetVideoInfoListener() {
                @Override
                public void onSuccess(List<Video> resultList, String pageToken) {
                    Log.d(TAG, "kth loadData() getYoutubeVideoInfo() onSuccess() resultList : " + resultList);
                    Log.d(TAG, "kth loadData() getYoutubeVideoInfo() onSuccess() pageToken : " + pageToken);
                    VideoInfoActivity.this.mPageToken = pageToken;

                    if(resultList!=null && !resultList.isEmpty()) {
                        mCurrentVideo = resultList.get(0);
                    }

                    if(mCurrentVideo != null && !TextUtils.isEmpty(mCurrentVideo.getId())) {
                        GlobalSingleton.getInstance().addVideoList(mCurrentVideo);
                    }

                    refreshListView();
                }

                @Override
                public void onFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubeVideoInfo() onFail() message : " + (e!=null ? e.getMessage() : ""));
                }

                @Override
                public void onAuthFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubeVideoInfo() onAuthFail()");
                    //startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);
                }
            });
        }
    }

    private void refreshListView(){
        if(mCurrentVideo == null){
            return;
        }

        VideoSnippet videoSnippet = mCurrentVideo.getSnippet();
        VideoContentDetails videoContentDetails = mCurrentVideo.getContentDetails();
        ContentRating contentRating = videoContentDetails.getContentRating();
        VideoStatistics videoStatistics = mCurrentVideo.getStatistics();

        ThumbnailDetails thumbnailDetails = videoSnippet.getThumbnails();
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
                        .into(iv_large);
            }
        }

        DateTime dateTime = videoSnippet.getPublishedAt();
        String description = videoSnippet.getDescription();

        tv_title.setText(videoSnippet.getTitle());
        tv_date.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN).format(new Date(dateTime.getValue())));

        if(TextUtils.isEmpty(description)){
            tv_desc.setText("");
            tv_desc.setVisibility(View.INVISIBLE);
        }else{
            tv_desc.setText(description);
            tv_desc.setVisibility(View.VISIBLE);

            LinkifyUtil.addLinks(tv_desc, LinkifyUtil.ALL);
        }

        List<String> tags = videoSnippet.getTags();
        if(tags == null || tags.isEmpty()){
            tv_tags.setText("");
            tv_tags.setVisibility(View.GONE);
        }else{
            StringBuilder sb = new StringBuilder();
            for(String tag : tags){
                sb.append(tag);
                sb.append(" ");
            }

            tv_tags.setText(mContext.getString(R.string.youtuber_videoinfo_tag, sb.toString()));
            tv_tags.setVisibility(View.VISIBLE);
        }

        if(contentRating != null){
            String rating = contentRating.getKmrbRating();
            tv_rating.setText(rating);
        }

        String duration = videoContentDetails.getDuration();
        duration = duration.replace("PT", "");
        duration = duration.replace("H", mContext.getString(R.string.youtuber_videoinfo_hour));
        duration = duration.replace("M", mContext.getString(R.string.youtuber_videoinfo_minute));
        duration = duration.replace("S", mContext.getString(R.string.youtuber_videoinfo_second));
        tv_duration.setText(duration);

        if(videoStatistics != null){
            BigInteger viewCount = videoStatistics.getViewCount();
            if(viewCount != null){
                tv_view.setText(mContext.getString(R.string.youtuber_videoinfo_view, StringUtil.getFormatedNumber(viewCount.toString())));
            }else{
                tv_view.setText(mContext.getString(R.string.youtuber_videoinfo_view, StringUtil.getFormatedNumber("0")));
            }

            BigInteger likeCount = videoStatistics.getLikeCount();
            if(likeCount != null){
                tv_like.setText(mContext.getString(R.string.youtuber_videoinfo_like, StringUtil.getFormatedNumber(likeCount.toString())));
            }else{
                tv_like.setText(mContext.getString(R.string.youtuber_videoinfo_like, StringUtil.getFormatedNumber("0")));
            }

            BigInteger dislikeCount = videoStatistics.getDislikeCount();
            if(dislikeCount != null){
                tv_dislike.setText(mContext.getString(R.string.youtuber_videoinfo_dislike, StringUtil.getFormatedNumber(dislikeCount.toString())));
            }else{
                tv_dislike.setText(mContext.getString(R.string.youtuber_videoinfo_dislike, StringUtil.getFormatedNumber("0")));
            }
        }
    }

    private void showShareVideoDialog(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=" + mCurrentVideo.getId());
        startActivity(Intent.createChooser(intent, mContext.getString(R.string.popup_youtube_video_share_title)));
    }

    private void showGoVideoDialog(){
        new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle(mContext.getString(R.string.popup_youtube_channel_title))
                .setMessage(mContext.getString(R.string.popup_youtube_video_msg))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + mCurrentVideo.getId()));
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
