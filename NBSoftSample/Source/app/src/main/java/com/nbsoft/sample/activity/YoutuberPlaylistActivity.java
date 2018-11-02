package com.nbsoft.sample.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.GlideApp;
import com.nbsoft.sample.R;
import com.nbsoft.sample.model.FirebaseDataItem;
import com.nbsoft.sample.youtube.YoutubeGetPlaylist;

import java.util.ArrayList;
import java.util.List;

public class YoutuberPlaylistActivity extends AppCompatActivity {
    public static final String TAG = YoutuberPlaylistActivity.class.getSimpleName();

    public static final int REQUEST_AUTHORIZATION = 1001;

    private Context mContext;

    private FirebaseDataItem mItem = null;
    private String mPageToken = "";

    private List<Playlist> mArrDataList;
    private boolean isMaxLoaded = false;

    private Toolbar toolbar;
    private TextView tv_toolbar_title;

    private ImageView iv_toolbar_drawer;
    private RelativeLayout rl_toolbar_drawer;
    private RelativeLayout rl_toolbar_info;

    private RecyclerView rv_contents;
    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private AppPreferences mPreferences;

    private DatabaseReference mDatabase;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_toolbar_drawer:
                    finish();
                    break;
                case R.id.rl_toolbar_info:
                    showChannelInfoPopup();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber_playlist);

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

        mContext = this;

        mPreferences = new AppPreferences(mContext);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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
        tv_toolbar_title.setText(mItem.getName());

        iv_toolbar_drawer = (ImageView) findViewById(R.id.iv_toolbar_drawer);
        iv_toolbar_drawer.setImageResource(R.drawable.btn_title_befor_nor);

        rl_toolbar_drawer = (RelativeLayout) findViewById(R.id.rl_toolbar_drawer);
        rl_toolbar_drawer.setClickable(true);
        rl_toolbar_drawer.setOnClickListener(onClickListener);

        rl_toolbar_info = (RelativeLayout) findViewById(R.id.rl_toolbar_info);
        rl_toolbar_info.setClickable(true);
        rl_toolbar_info.setOnClickListener(onClickListener);
    }

    private void loadData(){
        if(!isMaxLoaded){
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
                }

                @Override
                public void onFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylist() onFail() message : " + (e!=null ? e.getMessage() : ""));
                }

                @Override
                public void onAuthFail(Exception e) {
                    Log.d(TAG, "kth loadData() getYoutubePlaylist() onAuthFail()");
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

    private void showChannelInfoPopup(){

    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
        private Context context;
        private List<Playlist> dataArrayList;

        private int[] thumbnailHeight = {400, 450, 500};

        public RecyclerAdapter(Context context, List<Playlist> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<Playlist> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_playlist_list, parent, false);
            return new RecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            Playlist itemList = dataArrayList.get(position);
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
                    Intent intent = new Intent(YoutuberPlaylistActivity.this, YoutuberVideoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    Playlist playlist = (Playlist)v.getTag();
                    PlaylistSnippet snippet = playlist.getSnippet();

                    intent.putExtra("pid", playlist.getId());
                    intent.putExtra("pTitle", snippet.getTitle());
                    intent.putExtra("pDescription", snippet.getDescription());
                    startActivity(intent);
                    break;
            }
        }

        // Custom method to get a random number between a range
        protected int getRandomHeight(){
            int num = (int)(Math.random()*3);
            return thumbnailHeight[num];
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
}
