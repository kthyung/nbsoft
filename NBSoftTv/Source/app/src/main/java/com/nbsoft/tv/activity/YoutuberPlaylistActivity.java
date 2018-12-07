package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistContentDetails;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.GlideApp;
import com.nbsoft.tv.R;
import com.nbsoft.tv.model.FirebaseDataItem;
import com.nbsoft.tv.view.LoadingPopupManager;
import com.nbsoft.tv.youtube.YoutubeGetPlaylist;

import java.util.ArrayList;
import java.util.List;

public class YoutuberPlaylistActivity extends AppCompatActivity {
    public static final String TAG = YoutuberPlaylistActivity.class.getSimpleName();

    public static final int REQUEST_AUTHORIZATION = 1001;

    private Context mContext;
    private AppPreferences mPreferences;

    private FirebaseDataItem mItem = null;
    private String mPageToken = "";

    private List<Playlist> mArrDataList;
    private boolean isMaxLoaded = false;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private RecyclerView rv_contents;
    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

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
                case R.id.rl_toolbar_right:
                    showMenu(v);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber_playlist);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        Intent intent = getIntent();
        if(intent!=null){
            if(!intent.hasExtra("item")){
                Log.d(TAG, "kth onCreate() item is empty.");
                finish();
                return;
            }

            mItem = (FirebaseDataItem)intent.getSerializableExtra("item");

            mArrDataList = new ArrayList<Playlist>();

            initLayout();
            loadData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent!=null){
            if(!intent.hasExtra("item")){
                Log.d(TAG, "kth onNewIntent() item is empty.");
                finish();
                return;
            }

            mItem = (FirebaseDataItem)intent.getSerializableExtra("item");

            mArrDataList = new ArrayList<Playlist>();
            mPageToken = "";
            isMaxLoaded = false;

            initLayout();
            loadData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    loadData();
                }
                break;
        }
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
        tv_toolbar_title.setText(mItem.getName());

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.outline_arrow_back_ios_white_48);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(true);
        rl_toolbar_left.setOnClickListener(onClickListener);
        rl_toolbar_left.setVisibility(View.VISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.outline_more_vert_white_48);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(true);
        rl_toolbar_right.setOnClickListener(onClickListener);
        rl_toolbar_right.setVisibility(View.VISIBLE);
    }

    private void loadData(){
        if(!isMaxLoaded){
            LoadingPopupManager.getInstance(mContext).showLoading(YoutuberPlaylistActivity.this, true, "YoutuberPlaylistActivity");
            YoutubeGetPlaylist getPlayList = new YoutubeGetPlaylist(mContext);
            getPlayList.getYoutubePlaylist(mItem.getCid(), mPageToken, new YoutubeGetPlaylist.YoutubeGetPlaylistListener() {
                @Override
                public void onSuccess(List<Playlist> resultList, String pageToken) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylist() onSuccess() resultList : " + resultList);
                    Log.d(TAG, "kth loadData() getYoutubePlaylist() onSuccess() pageToken : " + pageToken);

                    YoutuberPlaylistActivity.this.mPageToken = pageToken;
                    if(resultList!=null && !resultList.isEmpty()) {
                        mArrDataList.addAll(resultList);

                        refreshListView();
                    }

                    if(TextUtils.isEmpty(pageToken)){
                        isMaxLoaded = true;
                    }

                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberPlaylistActivity");
                }

                @Override
                public void onFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylist() onFail() message : " + (e!=null ? e.getMessage() : ""));
                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberPlaylistActivity");
                }

                @Override
                public void onAuthFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylist() onAuthFail()");
                    LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberPlaylistActivity");
                    startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);
                }
            });
        }
    }

    private void refreshListView(){
        if(mAdapter == null){
            rv_contents = (RecyclerView) findViewById(R.id.rv_contents);

            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

            rv_contents.setLayoutManager(mLayoutManager);
            rv_contents.setItemAnimator(new DefaultItemAnimator());
            rv_contents.setNestedScrollingEnabled(false);
            rv_contents.setHasFixedSize(false);

            mAdapter = new RecyclerAdapter(mContext, mArrDataList);
            rv_contents.setAdapter(mAdapter);
        }else{
            mAdapter.setData(mArrDataList);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void showChannelInfo(){
        if(mItem == null){
            return;
        }

        Log.d(TAG, "kth showChannelInfo() mItem.getCid() : " + mItem.getCid());
        Intent intent = new Intent(YoutuberPlaylistActivity.this, ChannelInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("cid", mItem.getCid());
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
                    case R.id.menu_setting: {
                        Intent intent = new Intent(YoutuberPlaylistActivity.this, SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    return true;
                }

                return false;
            }
        });
        popup.inflate(R.menu.menu_youtuber_playlist);
        popup.show();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
        private Context context;
        private List<Playlist> dataArrayList;

        private int[] thumbnailHeight = {400, 450, 500, 550};

        public RecyclerAdapter(Context context, List<Playlist> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<Playlist> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_playlist_item, parent, false);
            return new RecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            Playlist itemList = dataArrayList.get(position);
            PlaylistContentDetails contentDetails = itemList.getContentDetails();
            PlaylistSnippet snippet = itemList.getSnippet();
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

            holder.iv_content.getLayoutParams().height = getRandomHeight();

            StringBuilder sb = new StringBuilder();
            sb.append(contentDetails.getItemCount());
            sb.append(mContext.getString(R.string.youtuber_playlist_count));
            holder.tv_count.setText(sb.toString());

            holder.tv_name.setText(snippet.getTitle());

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
                    Playlist playlist = (Playlist)v.getTag();
                    PlaylistContentDetails contentDetails = playlist.getContentDetails();
                    PlaylistSnippet snippet = playlist.getSnippet();

                    if(contentDetails.getItemCount() > 0){
                        Intent intent = new Intent(YoutuberPlaylistActivity.this, YoutuberVideoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("pid", playlist.getId());
                        intent.putExtra("pTitle", snippet.getTitle());
                        intent.putExtra("pDescription", snippet.getDescription());
                        startActivity(intent);
                    }

                    break;
            }
        }

        // Custom method to get a random number between a range
        protected int getRandomHeight(){
            int num = (int)(Math.random() * thumbnailHeight.length);
            return thumbnailHeight[num];
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_count;
            TextView tv_name;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_count = (TextView) itemView.findViewById(R.id.tv_count);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
