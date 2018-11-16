package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.nbsoft.tv.GlideApp;
import com.nbsoft.tv.R;
import com.nbsoft.tv.view.LoadingPopupManager;
import com.nbsoft.tv.youtube.YoutubeGetPlaylistItems;
import com.nbsoft.tv.youtube.YoutubeGetVideoInfo;

import java.util.ArrayList;
import java.util.List;

public class YoutuberVideoActivity extends YouTubeBaseActivity {
    public static final String TAG = YoutuberVideoActivity.class.getSimpleName();

    public static final int RECOVERY_DIALOG_REQUEST = 1;

    private Context mContext;

    private String mPid;
    private String mPtitle;
    private String mPdescription;

    private String mPlaylistItemsPageToken = "";
    private String mVideoInfoPageToken = "";

    private List<PlaylistItem> mArrDataList;
    private boolean isMaxLoaded = false;

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

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private LinearLayout ll_content;

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
                    showChannelInfo();

                    /*
                    Intent intent = new Intent(YoutuberActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                     */
                    break;
                case R.id.iv_auto:
                    toogleAutoPlay();
                    break;
                case R.id.btn_desc:
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber_video);

        mContext = this;

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

        iv_auto = (ImageView) findViewById(R.id.iv_auto);
        iv_auto.setImageResource(R.drawable.btn_toggle_on);
        iv_auto.setClickable(true);
        iv_auto.setOnClickListener(onClickListener);
        btn_desc = (ImageButton) findViewById(R.id.btn_desc);
        btn_desc.setClickable(true);
        btn_desc.setOnClickListener(onClickListener);
    }

    private void loadData(){
        if(!isMaxLoaded){
            LoadingPopupManager.getInstance(mContext).showLoading(YoutuberVideoActivity.this, true, "YoutuberVideoActivity");
            YoutubeGetPlaylistItems getPlaylistItems = new YoutubeGetPlaylistItems(mContext);
            getPlaylistItems.getYoutubePlaylistItems(mPid, mPlaylistItemsPageToken, new YoutubeGetPlaylistItems.YoutubeGetPlaylistItemsListener() {
                @Override
                public void onSuccess(List<PlaylistItem> resultList, String pageToken, int totalResults) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onSuccess() resultList : " + resultList);
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onSuccess() pageToken : " + pageToken);

                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberVideoActivity");
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
                }

                @Override
                public void onFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onFail() message : " + (e!=null ? e.getMessage() : ""));
                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberVideoActivity");
                }

                @Override
                public void onAuthFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onAuthFail()");
                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberVideoActivity");
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

                    mAdapter.notifyDataSetChanged();
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
    }

    private void toogleDescription(){

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

                /*
                if(mCurrentVideo != null){
                    VideoStatistics videoStatistics = mCurrentVideo.getStatistics();
                    if(videoStatistics != null){
                        StringBuilder sb = new StringBuilder();
                        sb.append(videoStatistics.getViewCount());
                        sb.append(mContext.getString(R.string.youtuber_video_view));
                        holder.tv_view.setText(sb.toString());
                        holder.tv_view.setVisibility(View.VISIBLE);
                    }

                    VideoContentDetails contentDetails = mCurrentVideo.getContentDetails();
                    if(contentDetails != null){
                        try {
                            holder.tv_time.setText(AppUtil.toTimeFromIso8601(contentDetails.getDuration()));
                            holder.tv_time.setVisibility(View.VISIBLE);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                */
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
        }

        @Override
        public void onPaused() {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onPaused()");
        }

        @Override
        public void onSeekTo(int endPositionMillis) {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerPlaybackEventListener onSeekTo() endPositionMillis : " + endPositionMillis);
        }
    }
}
