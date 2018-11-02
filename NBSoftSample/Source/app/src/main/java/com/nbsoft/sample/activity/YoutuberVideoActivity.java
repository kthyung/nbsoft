package com.nbsoft.sample.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.GlideApp;
import com.nbsoft.sample.R;
import com.nbsoft.sample.youtube.YoutubeGetPlaylistItems;

import java.util.ArrayList;
import java.util.List;

public class YoutuberVideoActivity extends YouTubeBaseActivity {
    public static final String TAG = YoutuberVideoActivity.class.getSimpleName();

    public static final int RECOVERY_DIALOG_REQUEST = 1;

    private Context mContext;

    private String mPid;
    private String mPtitle;
    private String mPdescription;

    private String mPageToken = "";

    private List<PlaylistItem> mArrDataList;
    private boolean isMaxLoaded = false;

    private RelativeLayout toolbar;
    private TextView tv_toolbar_title;

    private ImageView iv_toolbar_drawer;
    private RelativeLayout rl_toolbar_drawer;
    private RelativeLayout rl_toolbar_info;

    private RecyclerView rv_contents;
    private LinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private YouTubePlayerView mYouTubePlayerView;
    private YouTubePlayer mYouTubePlayer;

    private YoutubePlayerStateChangeListener stateChangeListener;
    private YoutubePlayerPlaybackEventListener playbackEventListener;

    private String mCurrentVideoId = "";

    private boolean isVideoEnd = false;
    private boolean isVideoPlaying = false;

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

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

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
        toolbar = (RelativeLayout) findViewById(R.id.toolbar);

        View viewToolbar = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_toolbar_main, null);
        toolbar.addView(viewToolbar, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        tv_toolbar_title = (TextView) viewToolbar.findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText(mPtitle);

        iv_toolbar_drawer = (ImageView) viewToolbar.findViewById(R.id.iv_toolbar_drawer);
        iv_toolbar_drawer.setImageResource(R.drawable.btn_title_befor_nor);

        rl_toolbar_drawer = (RelativeLayout) viewToolbar.findViewById(R.id.rl_toolbar_drawer);
        rl_toolbar_drawer.setClickable(true);
        rl_toolbar_drawer.setOnClickListener(onClickListener);

        rl_toolbar_info = (RelativeLayout) viewToolbar.findViewById(R.id.rl_toolbar_info);
        rl_toolbar_info.setClickable(false);
        rl_toolbar_info.setOnClickListener(null);
        rl_toolbar_info.setVisibility(View.INVISIBLE);
    }

    private void loadData(){
        if(!isMaxLoaded){
            YoutubeGetPlaylistItems getPlaylistItems = new YoutubeGetPlaylistItems(mContext);
            getPlaylistItems.getYoutubePlaylistItems(mPid, mPageToken, new YoutubeGetPlaylistItems.YoutubeGetPlaylistItemsListener() {
                @Override
                public void onSuccess(List<PlaylistItem> resultList, String pageToken, int totalResults) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onSuccess() resultList : " + resultList);
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onSuccess() pageToken : " + pageToken);
                    YoutuberVideoActivity.this.mPageToken = pageToken;
                    if(resultList!=null && !resultList.isEmpty()) {
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
                }

                @Override
                public void onAuthFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylistItems() onAuthFail()");
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
            mYouTubePlayer.loadVideo(resourceId.getVideoId());

            mCurrentVideoId = resourceId.getVideoId();
            mAdapter.notifyDataSetChanged();
        }
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_video_list, parent, false);
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
            ThumbnailDetails thumbnailDetails = snippet.getThumbnails();
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

            /*int[] attrs = new int[] { android.R.attr.selectedWeekBackgroundColor, android.R.attr.selectableItemBackground};
            TypedArray ta = obtainStyledAttributes(attrs);
            if(resourceId.getVideoId().equals(mCurrentVideoId)){
                Drawable drawableFromTheme = ta.getDrawable(0);
                holder.rl_main_layout.setBackground(drawableFromTheme);
            }else{
                Drawable drawableFromTheme = ta.getDrawable(1);
                holder.rl_main_layout.setBackground(drawableFromTheme);
            }*/

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
            TextView tv_name;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
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
            //mYouTubePlayer.loadVideo(playId);
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason reason) {
            if (reason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
                mYouTubePlayer = null;
            }

            Log.d(TAG, "kth YoutubePlayerStateChangeListener onError() reason : " + reason);
        }
    }

    public class YoutubePlayerPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {
        String playbackState = "NOT_PLAYING";
        String bufferingState = "";
        @Override
        public void onPlaying() {
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onPlaying()");
            isVideoPlaying = true;
        }

        @Override
        public void onBuffering(boolean isBuffering) {
            isVideoPlaying = false;

            Log.d(TAG, "kth YoutubePlayerStateChangeListener onBuffering() isBuffering : " + isBuffering);
        }

        @Override
        public void onStopped() {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onStopped()");
        }

        @Override
        public void onPaused() {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onPaused()");
        }

        @Override
        public void onSeekTo(int endPositionMillis) {
            isVideoPlaying = false;
            Log.d(TAG, "kth YoutubePlayerStateChangeListener onSeekTo() endPositionMillis : " + endPositionMillis);
        }
    }
}
