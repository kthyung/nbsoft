package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ContentRating;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
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
import com.nbsoft.tv.model.YoutuberHistory;
import com.nbsoft.tv.model.YoutuberHistoryItem;
import com.nbsoft.tv.view.LoadingPopupManager;
import com.nbsoft.tv.youtube.YoutubeGetPlaylistItems;
import com.nbsoft.tv.youtube.YoutubeGetVideoInfo;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class YoutuberVideoActivity extends YouTubeBaseActivity {
    public static final String TAG = YoutuberVideoActivity.class.getSimpleName();

    public static final int RECOVERY_DIALOG_REQUEST = 1;

    private Context mContext;
    private AppPreferences mPreferences;

    private String mPid;
    private String mPtitle;
    private String mPdescription;

    private String mPlaylistItemsPageToken = "";
    private String mVideoInfoPageToken = "";

    private List<PlaylistItem> mArrDataList;
    private boolean isMaxLoaded = false;
    private boolean isDataLoading = false;

    private Video mCurrentVideo;

    private String mCurrentChannelId = "";
    private String mCurrentVideoId = "";

    private YouTubePlayerView mYouTubePlayerView;
    private YouTubePlayer mYouTubePlayer;

    private YoutubePlayerStateChangeListener stateChangeListener;
    private YoutubePlayerPlaybackEventListener playbackEventListener;

    private boolean isVideoEnd = false;
    private boolean isVideoPlaying = false;

    private boolean isAutoPlay = true;

    private YoutuberHistory mHistory = new YoutuberHistory();
    private List<YoutuberHistoryItem> dataSparseArray = new ArrayList<YoutuberHistoryItem>();

    private long videoWatchingTime = 0L;
    private long videoWatchStartTime = 0L;
    private long videoWatchEndTime = 0L;

    private boolean isAnimationing = false;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private ScrollView sv_content;
    private TextView tv_title, tv_date, tv_duration, tv_rating, tv_view, tv_like, tv_dislike, tv_desc;

    private RelativeLayout rl_exit;

    private LinearLayout ll_content;

    private RelativeLayout rl_desc;
    private ImageView iv_auto;
    private ImageButton btn_desc;

    private RecyclerView rv_contents;
    private LinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_toolbar_left:
                    finish();
                    break;
                case R.id.rl_toolbar_right:
                    showMenu(v);
                    break;
                case R.id.iv_auto:
                    toogleAutoPlay();
                    break;
                case R.id.rl_desc:
                case R.id.btn_desc:
                case R.id.rl_exit:
                    toogleDescription();
                    break;
            }
        }
    };

    private YouTubePlayer.OnInitializedListener initializedListener = new YouTubePlayer.OnInitializedListener() {
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
            mYouTubePlayer = youTubePlayer;

            mYouTubePlayer.setPlayerStateChangeListener(stateChangeListener);
            mYouTubePlayer.setPlaybackEventListener(playbackEventListener);
            mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            mYouTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
            mYouTubePlayer.setOnFullscreenListener(fullscreenListener);

            if(!mArrDataList.isEmpty()){
                loadVideo(mArrDataList.get(0));
            }
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
            if (youTubeInitializationResult.isUserRecoverableError()) {
                youTubeInitializationResult.getErrorDialog(YoutuberVideoActivity.this, RECOVERY_DIALOG_REQUEST).show();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.video_error_str), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private YouTubePlayer.OnFullscreenListener fullscreenListener = new YouTubePlayer.OnFullscreenListener() {
        @Override
        public void onFullscreen(boolean full) {
        if(full){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber_video);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        Intent intent = getIntent();
        if(intent!=null){
            if(!intent.hasExtra("pid")){
                Log.d(TAG, "kth onCreate() pid is empty.");
                finish();
                return;
            }

            mPid = intent.getStringExtra("pid");
            mPtitle = intent.getStringExtra("pTitle");
            mPdescription = intent.getStringExtra("pDescription");

            mArrDataList = new ArrayList<PlaylistItem>();

            String youtuberHistory = mPreferences.getYoutuberHistory();
            if(!TextUtils.isEmpty(youtuberHistory)){
                mHistory = new Gson().fromJson(youtuberHistory, YoutuberHistory.class);
                if(mHistory!=null){
                    dataSparseArray = mHistory.getDataMap();
                }
            }

            initLayout();
            loadData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            mYouTubePlayerView.initialize(mContext.getString(R.string.google_api_key), initializedListener);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
    }

    private void initLayout(){
        RelativeLayout toolbar = (RelativeLayout) findViewById(R.id.toolbar);

        View viewToolbar = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_toolbar_main, null);
        toolbar.addView(viewToolbar, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        TextView tv_toolbar_title = (TextView) viewToolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(mPtitle);

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.btn_title_befor_nor);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(true);
        rl_toolbar_left.setOnClickListener(onClickListener);
        rl_toolbar_left.setVisibility(View.VISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.btn_title_option_nor);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(true);
        rl_toolbar_right.setOnClickListener(onClickListener);
        rl_toolbar_right.setVisibility(View.VISIBLE);

        ll_content = (LinearLayout) findViewById(R.id.ll_content);

        rl_desc = (RelativeLayout) findViewById(R.id.rl_desc);
        rl_desc.setClickable(true);
        rl_desc.setOnClickListener(onClickListener);

        iv_auto = (ImageView) findViewById(R.id.iv_auto);
        iv_auto.setImageResource(R.drawable.btn_toggle_on);
        iv_auto.setClickable(true);
        iv_auto.setOnClickListener(onClickListener);
        isAutoPlay = mPreferences.getAutoPlay();
        if(isAutoPlay){
            iv_auto.setImageResource(R.drawable.btn_toggle_on);
        }else{
            iv_auto.setImageResource(R.drawable.btn_toggle_off);
        }

        sv_content = (ScrollView) findViewById(R.id.sv_content);
        sv_content.setVisibility(View.GONE);

        rl_exit = (RelativeLayout) findViewById(R.id.rl_exit);
        rl_exit.setClickable(true);
        rl_exit.setOnClickListener(onClickListener);

        btn_desc = (ImageButton) findViewById(R.id.btn_desc);
        btn_desc.setClickable(true);
        btn_desc.setOnClickListener(onClickListener);

        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_date = (TextView)findViewById(R.id.tv_date);
        tv_duration = (TextView)findViewById(R.id.tv_duration);
        tv_rating = (TextView)findViewById(R.id.tv_rating);
        tv_view = (TextView)findViewById(R.id.tv_view);
        tv_like = (TextView)findViewById(R.id.tv_like);
        tv_dislike = (TextView)findViewById(R.id.tv_dislike);
        tv_desc = (TextView)findViewById(R.id.tv_desc);
    }

    private void loadData(){
        if(!isMaxLoaded){
            isDataLoading = true;
            LoadingPopupManager.getInstance(mContext).showLoading(YoutuberVideoActivity.this, true, "YoutuberVideoActivity");
            YoutubeGetPlaylistItems getPlaylistItems = new YoutubeGetPlaylistItems(mContext);
            getPlaylistItems.getYoutubePlaylistItems(mPid, mPlaylistItemsPageToken, new YoutubeGetPlaylistItems.YoutubeGetPlaylistItemsListener() {
                @Override
                public void onSuccess(List<PlaylistItem> resultList, String pageToken, int totalResults) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onSuccess() resultList : " + resultList);
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onSuccess() pageToken : " + pageToken);

                    YoutuberVideoActivity.this.mPlaylistItemsPageToken = pageToken;
                    if(resultList!=null && !resultList.isEmpty()) {
                        PlaylistItem itemList = resultList.get(0);
                        PlaylistItemSnippet snippet = itemList.getSnippet();
                        mCurrentChannelId = snippet.getChannelId();

                        mArrDataList.addAll(resultList);

                        refreshListView();
                    }

                    if(TextUtils.isEmpty(pageToken)){
                        isMaxLoaded = true;
                    }

                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberVideoActivity");
                    isDataLoading = false;
                }

                @Override
                public void onFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onFail() message : " + (e!=null ? e.getMessage() : ""));
                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberVideoActivity");
                    isDataLoading = false;
                }

                @Override
                public void onAuthFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onAuthFail()");
                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberVideoActivity");
                    isDataLoading = false;
                    //startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);
                }
            });
        }
    }

    private void refreshListView(){
        if(mAdapter == null){
            rv_contents = (RecyclerView) findViewById(R.id.rv_contents);

            mLayoutManager = new LinearLayoutManager(mContext);

            rv_contents.setLayoutManager(mLayoutManager);
            rv_contents.setItemAnimator(new DefaultItemAnimator());
            rv_contents.setNestedScrollingEnabled(false);
            rv_contents.setHasFixedSize(false);

            mAdapter = new RecyclerAdapter(mContext, mArrDataList);

            rv_contents.setAdapter(mAdapter);

            initVideo();
        }else{
            mAdapter.setData(mArrDataList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void refreshDescriptionView(){
        if(mCurrentVideo == null){
            return;
        }

        VideoSnippet videoSnippet = mCurrentVideo.getSnippet();
        VideoContentDetails videoContentDetails = mCurrentVideo.getContentDetails();
        ContentRating contentRating = videoContentDetails.getContentRating();
        VideoStatistics videoStatistics = mCurrentVideo.getStatistics();

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

    private void initVideo(){
        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        mYouTubePlayerView.initialize(mContext.getString(R.string.google_api_key), initializedListener);

        stateChangeListener = new YoutubePlayerStateChangeListener();
        playbackEventListener = new YoutubePlayerPlaybackEventListener();
    }

    private void loadVideo(PlaylistItem item){
        if(mYouTubePlayer != null){
            PlaylistItemSnippet snippet = item.getSnippet();
            ResourceId resourceId = snippet.getResourceId();
            mCurrentVideoId = resourceId.getVideoId();
            mYouTubePlayer.loadVideo(mCurrentVideoId);

            Video video = GlobalSingleton.getInstance().getVideoListItem(mCurrentVideoId);
            if(video != null){
                mCurrentVideo = video;
                mAdapter.notifyDataSetChanged();
                refreshDescriptionView();
            }else{
                YoutubeGetVideoInfo getVideoInfo = new YoutubeGetVideoInfo(mContext);
                getVideoInfo.getYoutubeVideoInfo(mCurrentVideoId, mVideoInfoPageToken, new YoutubeGetVideoInfo.YoutubeGetVideoInfoListener() {
                    @Override
                    public void onSuccess(List<Video> resultList, String pageToken) {
                        Log.d(TAG, "kth loadVideo() getYoutubeVideoInfo() onSuccess() resultList : " + resultList);
                        Log.d(TAG, "kth loadVideo() getYoutubeVideoInfo() onSuccess() pageToken : " + pageToken);
                        YoutuberVideoActivity.this.mVideoInfoPageToken = pageToken;

                        if(resultList!=null && !resultList.isEmpty()) {
                            mCurrentVideo = resultList.get(0);
                        }

                        GlobalSingleton.getInstance().addVideoList(mCurrentVideo);

                        mAdapter.notifyDataSetChanged();
                        refreshDescriptionView();
                    }

                    @Override
                    public void onFail(Exception e) {
                        Log.d(TAG, "kth loadVideo() getYoutubeVideoInfo() onFail() message : " + (e!=null ? e.getMessage() : ""));
                    }

                    @Override
                    public void onAuthFail(Exception e) {
                        Log.d(TAG, "kth loadVideo() getYoutubeVideoInfo() onAuthFail()");
                        //startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);
                    }
                });
            }
        }
    }

    private void playNextVideo(){
        if(!mArrDataList.isEmpty()){
            int index = -1;
            for(int i=0; i<mArrDataList.size(); i++){
                PlaylistItem item = mArrDataList.get(i);
                PlaylistItemSnippet snippet = item.getSnippet();
                ResourceId resourceId = snippet.getResourceId();
                String videoId = resourceId.getVideoId();
                if(mCurrentVideoId.equals(videoId)){
                    if(i != mArrDataList.size()-1){
                        index = i+1;
                    }
                }
            }

            if(index != -1) {
                loadVideo(mArrDataList.get(index));
            }
        }
    }

    private void toogleAutoPlay(){
        isAutoPlay = !isAutoPlay;
        if(isAutoPlay){
            iv_auto.setImageResource(R.drawable.btn_toggle_on);
        }else{
            iv_auto.setImageResource(R.drawable.btn_toggle_off);
        }

        mPreferences.setAutoPlay(isAutoPlay);
    }

    private void toogleDescription(){
        if(isAnimationing){
            return;
        }

        if(sv_content.getVisibility() == View.GONE){
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    sv_content.getHeight(),  // fromYDelta
                    0);                // toYDelta
            animate.setDuration(500);
            animate.setFillAfter(false);
            sv_content.startAnimation(animate);
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimationing = true;

                    sv_content.setVisibility(View.VISIBLE);
                    sv_content.scrollTo(0, 0);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isAnimationing = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }else{
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    0,                 // fromYDelta
                    sv_content.getHeight()); // toYDelta
            animate.setDuration(500);
            animate.setFillAfter(false);
            sv_content.startAnimation(animate);
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimationing = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isAnimationing = false;

                    sv_content.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }
    }

    public void slideUp(View view){

    }

    public void slideDown(View view){

    }

    public void showChannelInfo(){
        if(TextUtils.isEmpty(mCurrentChannelId)){
            return;
        }

        Log.d(TAG, "kth showChannelInfo() mCurrentChannelId : " + mCurrentChannelId);
        Intent intent = new Intent(YoutuberVideoActivity.this, ChannelInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("cid", mCurrentChannelId);
        startActivity(intent);
    }

    public void showVideoInfo(){
        if(TextUtils.isEmpty(mCurrentVideoId)){
            return;
        }

        Log.d(TAG, "kth showVideoInfo() mCurrentVideoId : " + mCurrentVideoId);
        Intent intent = new Intent(YoutuberVideoActivity.this, VideoInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("vid", mCurrentVideoId);
        startActivity(intent);
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_channelinfo:
                        showChannelInfo();
                        return true;
                    case R.id.menu_videoinfo:
                        showVideoInfo();
                        return true;
                    case R.id.menu_setting: {
                        Intent intent = new Intent(YoutuberVideoActivity.this, SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    return true;
                }

                return false;
            }
        });
        popup.inflate(R.menu.menu_youtuber_video);
        popup.show();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
        Context context;
        List<PlaylistItem> dataArrayList;

        public RecyclerAdapter(Context context, List<PlaylistItem> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<PlaylistItem> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_video_item, parent, false);
            return new RecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            PlaylistItem itemList = dataArrayList.get(position);
            PlaylistItemSnippet snippet = itemList.getSnippet();
            ResourceId resourceId = snippet.getResourceId();
            String videoId = resourceId.getVideoId();
            ThumbnailDetails thumbnailDetails = snippet.getThumbnails();
            holder.iv_content.setImageResource(R.color.color_dddddd);
            if(thumbnailDetails != null){
                Thumbnail thumbnail = thumbnailDetails.getHigh();
                if(thumbnail == null){
                    thumbnail = thumbnailDetails.getMedium();
                }
                if(thumbnail == null){
                    thumbnail = thumbnailDetails.getStandard();
                }
                if(thumbnail != null){
                    GlideApp.with(context)
                            .load(thumbnail.getUrl())
                            .into(holder.iv_content);
                }
            }

            holder.tv_name.setText(snippet.getTitle());

            holder.tv_view.setText("");
            holder.tv_view.setVisibility(View.GONE);
            holder.tv_time.setText("");
            holder.tv_time.setVisibility(View.GONE);

            if(videoId.equals(mCurrentVideoId)){
                holder.rl_main_layout.setBackgroundResource(R.color.color_eeeeee);
            }else{
                TypedArray ta = obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground});
                Drawable drawableFromTheme = ta.getDrawable(0);
                holder.rl_main_layout.setBackground(drawableFromTheme);
            }

            holder.rl_main_layout.setTag(itemList);
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);

            if(position == getItemCount()-1){
                loadData();
            }
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    PlaylistItem itemList = (PlaylistItem)v.getTag();
                    PlaylistItemSnippet snippet = itemList.getSnippet();
                    ResourceId resourceId = snippet.getResourceId();
                    if(!resourceId.getVideoId().equals(mCurrentVideoId)){
                        loadVideo(itemList);
                    }

                    break;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_view;
            TextView tv_time;
            TextView tv_name;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_view = (TextView) itemView.findViewById(R.id.tv_view);
                tv_time = (TextView) itemView.findViewById(R.id.tv_time);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }

    public class YoutubePlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        String playerState = "UNINITIALIZED";

        @Override
        public void onLoading() {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onLoading()");
        }

        @Override
        public void onLoaded(String videoId) {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onLoaded() videoId : " + videoId);
            if(!isVideoEnd){
                mYouTubePlayer.play();
            }
        }

        @Override
        public void onAdStarted() {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onAdStarted()");
            if(!isVideoEnd){
                mYouTubePlayer.play();
            }
        }

        @Override
        public void onVideoStarted() {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onVideoStarted()");

            //isVideoEnd = false;
        }

        @Override
        public void onVideoEnded() {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onVideoEnded()");
            if(!isVideoEnd){
                //reserve_connection();
            }

            isVideoEnd = true;
            if(isAutoPlay){
                playNextVideo();
            }
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason reason) {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onError() reason : " + reason);
            if(reason == YouTubePlayer.ErrorReason.NOT_PLAYABLE){

            }else if (reason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
                mYouTubePlayer = null;
            }
        }
    }

    public class YoutubePlayerPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {
        String playbackState = "NOT_PLAYING";
        String bufferingState = "";
        @Override
        public void onPlaying() {
            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onPlaying()");
            isVideoPlaying = true;
            videoWatchStartTime = System.currentTimeMillis();
        }

        @Override
        public void onBuffering(boolean isBuffering) {
            isVideoPlaying = false;

            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onBuffering() isBuffering : " + isBuffering);
        }

        @Override
        public void onStopped() {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onStopped()");

            if(videoWatchStartTime > 0L) {
                checkWatchingTime();
                videoWatchStartTime = 0L;
                videoWatchingTime = 0L;
            }
        }

        @Override
        public void onPaused() {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onPaused()");

            if(videoWatchStartTime > 0L) {
                checkWatchingTime();
            }
        }

        @Override
        public void onSeekTo(int endPositionMillis) {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onSeekTo() endPositionMillis : " + endPositionMillis);
        }

        public void checkWatchingTime(){
            videoWatchEndTime = System.currentTimeMillis();
            videoWatchingTime += (videoWatchEndTime-videoWatchStartTime);
            if(videoWatchingTime > 1000 * 60){
                saveHistory();
                videoWatchStartTime = 0L;
                videoWatchingTime = 0L;
            }

            videoWatchEndTime = 0L;
        }

        public void saveHistory(){
            Log.d(TAG, "kth saveHistory()");

            if(dataSparseArray == null){
                dataSparseArray = new ArrayList<YoutuberHistoryItem>();
            }

            VideoSnippet videoSnippet = mCurrentVideo.getSnippet();
            VideoContentDetails videoContentDetails = mCurrentVideo.getContentDetails();
            ThumbnailDetails thumbnailDetails = videoSnippet.getThumbnails();
            String thumbnailStr = "";
            if(thumbnailDetails != null){
                Thumbnail thumbnail = thumbnailDetails.getHigh();
                if(thumbnail == null){
                    thumbnail = thumbnailDetails.getMedium();
                }
                if(thumbnail == null){
                    thumbnail = thumbnailDetails.getStandard();
                }
                if(thumbnail != null){
                    thumbnailStr = thumbnail.getUrl();
                }
            }

            Calendar nowCalendar = Calendar.getInstance(Locale.KOREAN);
            Calendar calendar = Calendar.getInstance(Locale.KOREAN);
            calendar.clear();
            calendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, nowCalendar.get(Calendar.DAY_OF_MONTH));

            YoutuberHistoryItem item = new YoutuberHistoryItem();
            item.setPid(mPid);
            item.setpTitle(mPtitle);
            item.setpDescription(mPdescription);
            item.setThumbnail(thumbnailStr);
            item.setName(videoSnippet.getTitle());
            item.setVid(mCurrentVideoId);
            item.setHistoryDate(calendar.getTimeInMillis());

            dataSparseArray.add(item);
            mHistory.setDataMap(dataSparseArray);
            mPreferences.setYoutuberHistory(new Gson().toJson(mHistory));
        }
    }
}
