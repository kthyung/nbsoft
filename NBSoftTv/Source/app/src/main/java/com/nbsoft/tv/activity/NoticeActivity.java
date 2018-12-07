package com.nbsoft.tv.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.GlideApp;
import com.nbsoft.tv.GlobalSingleton;
import com.nbsoft.tv.R;
import com.nbsoft.tv.model.FirebaseDataItem;
import com.nbsoft.tv.model.FirebaseNoticeItem;
import com.nbsoft.tv.model.FirebaseRequestItem;
import com.nbsoft.tv.model.YoutuberHistoryItem;
import com.nbsoft.tv.model.YoutuberRequest;

import java.util.ArrayList;
import java.util.List;

public class NoticeActivity extends AppCompatActivity {
    public static final String TAG = NoticeActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;

    private List<FirebaseNoticeItem> mArrDataList;

    private RecyclerView rv_contents;
    private LinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

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
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
        loadData();
        refreshListView();
    }

    @Override
    protected void onDestroy() {
        for(FirebaseNoticeItem item : mArrDataList){
            item.setExpand(false);
        }

        super.onDestroy();
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
        tv_toolbar_title.setText(mContext.getString(R.string.title_notice));

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.outline_arrow_back_ios_white_48);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(true);
        rl_toolbar_left.setOnClickListener(onClickListener);
        rl_toolbar_left.setVisibility(View.VISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.outline_more_vert_white_48);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(false);
        rl_toolbar_right.setOnClickListener(null);
        rl_toolbar_right.setVisibility(View.INVISIBLE);
    }

    private void loadData(){
        mArrDataList = new ArrayList<FirebaseNoticeItem>();

        List<FirebaseNoticeItem> notice = GlobalSingleton.getInstance().getNoticeList();
        if(notice != null){
            mArrDataList = notice;
        }
    }

    private void refreshListView(){
        if(mAdapter == null){
            rv_contents = (RecyclerView) findViewById(R.id.rv_contents);

            mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);

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
        }else{
            mAdapter.setData(mArrDataList);
            mAdapter.notifyDataSetChanged();
        }
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
        Context context;
        List<FirebaseNoticeItem> dataArrayList;

        public RecyclerAdapter(Context context, List<FirebaseNoticeItem> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<FirebaseNoticeItem> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_notice_item, parent, false);
            return new RecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            FirebaseNoticeItem itemList = dataArrayList.get(position);

            holder.tv_title.setText(itemList.getTitle());
            holder.tv_date.setText(itemList.getDate());
            holder.tv_content.setText(itemList.getContent());

            holder.rl_main_layout.setTag(itemList);
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);

            if(itemList.isExpand()){
                holder.rl_main_content.setVisibility(View.VISIBLE);
            }else{
                holder.rl_main_content.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout :
                    FirebaseNoticeItem item = (FirebaseNoticeItem)v.getTag();
                    item.setExpand(!item.isExpand());
                    notifyDataSetChanged();
                    break;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            YoutuberBookmarkActivity.RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            LinearLayout rl_main_content;
            TextView tv_title;
            TextView tv_date;
            TextView tv_content;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                rl_main_content = (LinearLayout) itemView.findViewById(R.id.rl_main_content);
                tv_title = (TextView) itemView.findViewById(R.id.tv_title);
                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
                tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
