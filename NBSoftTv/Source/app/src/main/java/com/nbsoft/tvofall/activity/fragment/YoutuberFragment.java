package com.nbsoft.tvofall.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.GlideApp;
import com.nbsoft.tvofall.R;
import com.nbsoft.tvofall.activity.YoutuberActivity;
import com.nbsoft.tvofall.activity.YoutuberPlaylistActivity;
import com.nbsoft.tvofall.model.FirebaseDataItem;
import com.nbsoft.tvofall.view.FastScroller;
import com.nbsoft.tvofall.view.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class YoutuberFragment extends Fragment {
    public static final String TAG = YoutuberFragment.class.getSimpleName();

    private YoutuberActivity mActivity;
    private AppPreferences mPreferences;

    private List<FirebaseDataItem> mArrFilteredDataList;

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
        View view =  inflater.inflate(R.layout.fragment_youtuber, container, false);

        rl_top = (RelativeLayout) view.findViewById(R.id.rl_top);
        rl_top.setClickable(true);
        rl_top.setOnClickListener(onClickListener);
        rl_top.setVisibility(View.VISIBLE);

        Bundle bundle = getArguments();
        if(bundle != null) {
            mArrFilteredDataList = (ArrayList<FirebaseDataItem>)bundle.getSerializable("data");
        }

        refreshListView(view);

        return view;
    }

    public void refreshListView(){
        refreshListView(getView());
    }

    public void refreshListView(View view){
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

            mAdapter = new RecyclerAdapter(mActivity, mArrFilteredDataList);

            rv_contents.setAdapter(mAdapter);

            fastScroller = (FastScroller) view.findViewById(R.id.fs_contents);
            fastScroller.setRecyclerView(rv_contents);
        }else{
            mAdapter.setData(mArrFilteredDataList);
            mAdapter.notifyDataSetChanged();
        }
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_item, parent, false);
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

            holder.rl_main_layout.setTag(itemList);
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);
            holder.rl_main_layout.setOnLongClickListener(this);

            holder.rl_info.setTag(itemList);
            holder.rl_info.setClickable(false);
            holder.rl_info.setVisibility(View.GONE);
            holder.rl_info.setOnClickListener(null);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    Intent intent = new Intent(mActivity, YoutuberPlaylistActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("item", (FirebaseDataItem)v.getTag());
                    startActivity(intent);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.i(TAG, "kth ViewHolder onLongClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    mActivity.showChannelInfo((FirebaseDataItem)v.getTag());
                    return true;
            }

            return false;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_name;
            RelativeLayout rl_info;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                rl_info = (RelativeLayout) itemView.findViewById(R.id.rl_info);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
