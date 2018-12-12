package com.nbsoft.tvofall.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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

import com.google.gson.Gson;
import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.GlideApp;
import com.nbsoft.tvofall.R;
import com.nbsoft.tvofall.model.YoutuberHistory;
import com.nbsoft.tvofall.model.YoutuberHistoryItem;
import com.nbsoft.tvofall.view.FastScroller;
import com.nbsoft.tvofall.view.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class YoutuberHistoryActivity extends AppCompatActivity {
    public static final String TAG = YoutuberHistoryActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private List<YoutuberHistoryItem> mArrDataList;

    private YoutuberHistory mHistory = new YoutuberHistory();
    private List<YoutuberHistoryItem> dataArray = new ArrayList<YoutuberHistoryItem>();

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private RecyclerView rv_contents;
    private ScrollingLinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;
    private FastScroller fastScroller;

    private RelativeLayout rl_top;

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
        setContentView(R.layout.activity_youtuber_history);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        loadData();
        refreshListView();
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
        tv_toolbar_title.setText(mContext.getString(R.string.video_history_title));

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
        mArrDataList = new ArrayList<YoutuberHistoryItem>();

        String youtuberHistory = mPreferences.getYoutuberHistory();
        if(!TextUtils.isEmpty(youtuberHistory)){
            mHistory = new Gson().fromJson(youtuberHistory, YoutuberHistory.class);
            if(mHistory!=null){
                dataArray = mHistory.getDataMap();
                mArrDataList.addAll(dataArray);
            }
        }
    }

    private void refreshListView(){
        if(mAdapter == null){
            rv_contents = (RecyclerView) findViewById(R.id.rv_contents);

            mLayoutManager = new ScrollingLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false, 1000);

            rv_contents.setLayoutManager(mLayoutManager);
            rv_contents.setItemAnimator(new DefaultItemAnimator());
            rv_contents.setNestedScrollingEnabled(false);
            rv_contents.setHasFixedSize(false);
            rv_contents.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                        //rl_top.setVisibility(View.VISIBLE);
                    }else if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        //rl_top.setVisibility(View.GONE);
                    }
                }
            });

            mAdapter = new RecyclerAdapter(mContext, mArrDataList);

            rv_contents.setAdapter(mAdapter);

            fastScroller = (FastScroller) findViewById(R.id.fs_contents);
            fastScroller.setRecyclerView(rv_contents);

            rl_top = (RelativeLayout) findViewById(R.id.rl_top);
            rl_top.setClickable(true);
            rl_top.setOnClickListener(onClickListener);
            rl_top.setVisibility(View.VISIBLE);
        }else{
            mAdapter.setData(mArrDataList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showDeleteHistoryDialog(){
        new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle(mContext.getString(R.string.video_history_title))
                .setMessage(mContext.getString(R.string.youtuber_bookmark_allremove))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataArray.clear();
                        mHistory.setDataMap(dataArray);
                        mPreferences.setYoutuberHistory(new Gson().toJson(mHistory));

                        loadData();
                        refreshListView();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void showVideoInfo(YoutuberHistoryItem item){
        if(item == null){
            return;
        }

        Log.d(TAG, "kth showVideoInfo() item.getVid() : " + item.getVid());
        Intent intent = new Intent(YoutuberHistoryActivity.this, VideoInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("vid", item.getVid());
        startActivity(intent);
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_bookmark:
                        showDeleteHistoryDialog();
                        return true;
                }

                return false;
            }
        });
        popup.inflate(R.menu.menu_youtuber_bookmark);
        popup.show();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener{
        Context context;
        List<YoutuberHistoryItem> dataArrayList;

        public RecyclerAdapter(Context context, List<YoutuberHistoryItem> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<YoutuberHistoryItem> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_history_item, parent, false);
            return new RecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            YoutuberHistoryItem itemList = dataArrayList.get(position);

            GlideApp.with(context)
                    .load(itemList.getThumbnail())
                    .into(holder.iv_content);

            holder.tv_view.setVisibility(View.GONE);
            holder.tv_time.setVisibility(View.GONE);

            holder.tv_name.setText(itemList.getName());

            holder.rl_main_layout.setTag(itemList);
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);
            holder.rl_main_layout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout :
                    YoutuberHistoryItem item = (YoutuberHistoryItem)v.getTag();

                    Intent intent = new Intent(YoutuberHistoryActivity.this, YoutuberVideoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("pid", item.getPid());
                    intent.putExtra("pTitle", item.getpTitle());
                    intent.putExtra("pDescription", item.getpDescription());
                    startActivity(intent);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.i(TAG, "kth ViewHolder onLongClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    showVideoInfo((YoutuberHistoryItem)v.getTag());
                    return true;
            }

            return false;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            YoutuberBookmarkActivity.RecyclerAdapter.ViewHolder holder;

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


}
