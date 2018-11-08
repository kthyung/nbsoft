package com.nbsoft.sample.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.GlideApp;
import com.nbsoft.sample.R;
import com.nbsoft.sample.activity.YoutuberActivity;
import com.nbsoft.sample.activity.YoutuberPlaylistActivity;
import com.nbsoft.sample.model.FirebaseDataItem;
import com.nbsoft.sample.model.YoutuberBookmark;
import com.nbsoft.sample.view.FastScroller;
import com.nbsoft.sample.view.ScrollingLinearLayoutManager;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class YoutuberBookmarkFragment extends Fragment {
    public static final String TAG = YoutuberBookmarkFragment.class.getSimpleName();

    private YoutuberActivity mActivity;

    private List<FirebaseDataItem> mArrDataList;

    private YoutuberBookmark mBookMark = new YoutuberBookmark();
    private HashMap<String, FirebaseDataItem> mDataHashMap = new HashMap<String, FirebaseDataItem>();

    private RecyclerView rv_contents;
    private ScrollingLinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;
    private FastScroller fastScroller;

    private RelativeLayout rl_top;

    private AppPreferences mPreferences;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_top:
                    if(rv_contents!=null){
                        rv_contents.smoothScrollToPosition(0);
                    }

                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreate()");
        super.onCreate(savedInstanceState);

        mActivity = (YoutuberActivity) getActivity();
        mPreferences = new AppPreferences(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreateView()");
        View view =  inflater.inflate(R.layout.fragment_youtuber_bookmark, container, false);

        rl_top = (RelativeLayout) view.findViewById(R.id.rl_top);
        rl_top.setClickable(true);
        rl_top.setOnClickListener(onClickListener);
        rl_top.setVisibility(View.VISIBLE);

        refreshListView(view);

        return view;
    }

    public void refreshListView(View view){
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

        if(mAdapter == null){
            rv_contents = (RecyclerView) view.findViewById(R.id.rv_contents);

            mLayoutManager = new ScrollingLinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false, 1000);

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

            mAdapter = new RecyclerAdapter(mActivity, mArrDataList);

            rv_contents.setAdapter(mAdapter);

            fastScroller = (FastScroller) view.findViewById(R.id.fs_contents);
            fastScroller.setRecyclerView(rv_contents);
        }else{
            mAdapter.setData(mArrDataList);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void refreshListView(){
        refreshListView(getView());
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
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
            sb.append(mActivity.getString(R.string.youtuber_bookmark_date));
            sb.append(" ");
            sb.append(new SimpleDateFormat("yyyy.MM.dd  a hh:mm", Locale.KOREAN).format(new Date(itemList.getBookmarkDate())));
            holder.tv_date.setText(sb.toString());

            holder.rl_main_layout.setTag(itemList);
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    Intent intent = new Intent(mActivity, YoutuberPlaylistActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("item", (FirebaseDataItem)v.getTag());
                    startActivity(intent);
                    break;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            YoutuberTotalFragment.RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_name;
            TextView tv_date;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
