package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.api.services.youtube.model.Channel;
import com.google.gson.Gson;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.GlideApp;
import com.nbsoft.tv.R;
import com.nbsoft.tv.model.FirebaseDataItem;
import com.nbsoft.tv.model.YoutuberBookmark;
import com.nbsoft.tv.view.FastScroller;
import com.nbsoft.tv.view.ScrollingLinearLayoutManager;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class YoutuberBookmarkActivity extends AppCompatActivity {
    public static final String TAG = YoutuberBookmarkActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private List<FirebaseDataItem> mArrDataList;

    private YoutuberBookmark mBookMark = new YoutuberBookmark();
    private HashMap<String, FirebaseDataItem> mDataHashMap = new HashMap<String, FirebaseDataItem>();

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
        setContentView(R.layout.activity_youtuber_bookmark);

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
        tv_toolbar_title.setText(mContext.getString(R.string.youtuber_bookmark_title));

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
        mArrDataList = new ArrayList<FirebaseDataItem>();

        String youtuberBookmark = mPreferences.getYoutuberBookmark();
        if(!TextUtils.isEmpty(youtuberBookmark)){
            mBookMark = new Gson().fromJson(youtuberBookmark, YoutuberBookmark.class);
            if(mBookMark!=null){
                mDataHashMap = mBookMark.getDataMap();
                for(String key : mDataHashMap.keySet()){
                    mArrDataList.add(mDataHashMap.get(key));
                }

                Collections.sort(mArrDataList, new Comparator<FirebaseDataItem>() {
                    private final Collator collator = Collator.getInstance(new Locale("ko"));

                    @Override
                    public int compare(FirebaseDataItem o1, FirebaseDataItem o2) {
                        try{
                            Long date1 = o1.getBookmarkDate();
                            Long date2 = o2.getBookmarkDate();

                            if(date1 < date2){
                                return 1;
                            }else if(date1 > date2){
                                return -1;
                            }else{
                                return 0;
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                        return 0;
                    }
                });
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

    private void showDeleteBookmarkDialog(){
        new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle(mContext.getString(R.string.youtuber_bookmark_title))
                .setMessage(mContext.getString(R.string.youtuber_bookmark_allremove))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDataHashMap.clear();
                        mBookMark.setDataMap(mDataHashMap);
                        mPreferences.setYoutuberBookmark(new Gson().toJson(mBookMark));

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

    public void showChannelInfo(FirebaseDataItem item){
        if(item == null){
            return;
        }

        Log.d(TAG, "kth showChannelInfo() item.getCid() : " + item.getCid());
        Intent intent = new Intent(YoutuberBookmarkActivity.this, ChannelInfoActivity.class);
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
                    case R.id.menu_bookmark:
                        showDeleteBookmarkDialog();
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
        List<FirebaseDataItem> dataArrayList;

        public RecyclerAdapter(Context context, List<FirebaseDataItem> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<FirebaseDataItem> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_bookmark_item, parent, false);
            return new RecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            FirebaseDataItem itemList = dataArrayList.get(position);

            GlideApp.with(context)
                    .load(itemList.getThumbnail())
                    .into(holder.iv_content);

            holder.tv_name.setText(itemList.getName());

            StringBuilder sb = new StringBuilder();
            sb.append(mContext.getString(R.string.youtuber_bookmark_date));
            sb.append(" ");
            sb.append(new SimpleDateFormat("yyyy.MM.dd  a hh:mm", Locale.KOREAN).format(new Date(itemList.getBookmarkDate())));
            holder.tv_date.setText(sb.toString());

            holder.rl_main_layout.setTag(itemList);
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);
            holder.rl_main_layout.setOnLongClickListener(this);

            holder.rl_bookmark.setTag(itemList);
            holder.rl_bookmark.setClickable(true);
            holder.rl_bookmark.setVisibility(View.VISIBLE);
            holder.rl_bookmark.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout :
                    Intent intent = new Intent(mContext, YoutuberPlaylistActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("item", (FirebaseDataItem)v.getTag());
                    startActivity(intent);
                    break;
                case R.id.rl_bookmark :
                    FirebaseDataItem itemList = (FirebaseDataItem)v.getTag();
                    mDataHashMap.remove(itemList.getCid());
                    mBookMark.setDataMap(mDataHashMap);
                    mPreferences.setYoutuberBookmark(new Gson().toJson(mBookMark));

                    loadData();
                    refreshListView();

                    Toast.makeText(mContext, mContext.getString(R.string.youtuber_bookmark_remove, itemList.getName()), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.i(TAG, "kth ViewHolder onLongClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    showChannelInfo((FirebaseDataItem)v.getTag());
                    return true;
            }

            return false;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_name;
            TextView tv_date;
            RelativeLayout rl_bookmark;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
                rl_bookmark = (RelativeLayout) itemView.findViewById(R.id.rl_bookmark);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
