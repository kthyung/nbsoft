package com.nbsoft.sample.activity.fragment;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.GlideApp;
import com.nbsoft.sample.R;
import com.nbsoft.sample.activity.YoutuberActivity;
import com.nbsoft.sample.activity.YoutuberPlaylistActivity;
import com.nbsoft.sample.model.FirebaseDataItem;
import com.nbsoft.sample.model.FirebaseItem;
import com.nbsoft.sample.model.YoutuberBookmark;
import com.nbsoft.sample.view.FastScroller;
import com.nbsoft.sample.view.ScrollingLinearLayoutManager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class YoutuberTotalFragment extends Fragment {
    public static final String TAG = YoutuberTotalFragment.class.getSimpleName();

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private YoutuberActivity mActivity;
    private DatabaseReference mDatabase;

    private List<FirebaseDataItem> mArrDataList;
    private List<FirebaseDataItem> mArrFilteredDataList;

    private YoutuberBookmark mBookMark = new YoutuberBookmark();
    private HashMap<String, FirebaseDataItem> mDataHashMap = new HashMap<String, FirebaseDataItem>();

    private RecyclerView rv_contents;
    private ScrollingLinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;
    private FastScroller fastScroller;

    private int mCurrentType = -1;

    private String[] mArrYoutuberType;

    private Button btn_type;

    private RelativeLayout rl_top;

    private GoogleAccountCredential mCredential;

    private AppPreferences mPreferences;

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            Log.d(TAG, "kth ValueEventListener onDataChange() DataSnapshot : " + (dataSnapshot!=null ? dataSnapshot.toString() : "null"));
            try{
                FirebaseItem obj = dataSnapshot.getValue(FirebaseItem.class);
                if(obj != null){
                    mArrDataList = obj.getData();
                    Collections.sort(mArrDataList, new Comparator<FirebaseDataItem>() {
                        private final Collator collator = Collator.getInstance(new Locale("ko"));

                        @Override
                        public int compare(FirebaseDataItem o1, FirebaseDataItem o2) {
                            boolean isLeftNumber = true;
                            boolean isRightNumber = true;

                            char leftChar = o1.getName().charAt(0);
                            char rightChar = o2.getName().charAt(0);
                            if(leftChar < '0' || leftChar > '9'){
                                isLeftNumber = false;
                            }

                            if(rightChar < '0' || rightChar > '9'){
                                isRightNumber = false;
                            }

                            if(isLeftNumber && !isRightNumber) {
                                return 1;
                            } else if(!isLeftNumber && isRightNumber) {
                                return -1;
                            } else {
                                return collator.compare(o1.getName(), o2.getName());
                            }
                        }
                    });

                    refreshListView();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.d(TAG, "kth ValueEventListener onCancelled() DatabaseError : ", databaseError.toException());
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_top:
                    if(rv_contents!=null){
                        rv_contents.smoothScrollToPosition(0);
                    }

                    break;
                case R.id.btn_type:
                    showTypeSelectDialog();
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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mArrYoutuberType = mActivity.getResources().getStringArray(R.array.arr_youtuber_type);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreateView()");
        View view =  inflater.inflate(R.layout.fragment_youtuber_total, container, false);

        rl_top = (RelativeLayout) view.findViewById(R.id.rl_top);
        rl_top.setClickable(true);
        rl_top.setOnClickListener(onClickListener);
        rl_top.setVisibility(View.VISIBLE);

        btn_type = (Button) view.findViewById(R.id.btn_type);
        btn_type.setOnClickListener(onClickListener);
        btn_type.setVisibility(View.GONE);

        initGoogleAccount();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "kth onActivityResult() REQUEST_GOOGLE_PLAY_SERVICES resultCode != RESULT_OK");
                } else {
                    loadData();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    Log.d(TAG, "kth onActivityResult() REQUEST_ACCOUNT_PICKER resultCode == RESULT_OK");
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mPreferences.setGoogleAccountName(accountName);
                        mCredential.setSelectedAccountName(accountName);
                        loadData();
                    }
                }
                break;
        }
    }

    public void loadData(){
        mDatabase.addValueEventListener(valueEventListener);

        String youtuberBookmark = mPreferences.getYoutuberBookmark();
        if(!TextUtils.isEmpty(youtuberBookmark)){
            mBookMark = new Gson().fromJson(youtuberBookmark, YoutuberBookmark.class);
            if(mBookMark!=null){
                mDataHashMap = mBookMark.getDataMap();
            }
        }
    }

    public void refreshListView(){
        refreshListView(0);
    }

    public void refreshListView(int type){
        if(mCurrentType == type){
            return;
        }

        mCurrentType = type;
        mArrFilteredDataList = new ArrayList<FirebaseDataItem>();
        if(type == 0){
            mArrFilteredDataList.addAll(mArrDataList);
        }else{
            for(FirebaseDataItem item : mArrDataList){
                String typeStr = AppUtil.getTypeToString(type);
                if(item.getType().equals(typeStr)){
                    mArrFilteredDataList.add(item);
                }
            }
        }

        if(mAdapter == null){
            rv_contents = (RecyclerView) getView().findViewById(R.id.rv_contents);

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

            fastScroller = (FastScroller) getView().findViewById(R.id.fs_contents);
            fastScroller.setRecyclerView(rv_contents);
        }else{
            mAdapter.setData(mArrFilteredDataList);
            mAdapter.notifyDataSetChanged();
        }

        btn_type.setText(mArrYoutuberType[type]);
        btn_type.setVisibility(View.VISIBLE);
    }

    private void showTypeSelectDialog(){
        new AlertDialog.Builder(mActivity)
                .setItems(mArrYoutuberType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshListView(which);
                        if(rv_contents!=null){
                            rv_contents.scrollToPosition(0);
                        }
                    }
                })
                .setCancelable(true)
                .setNegativeButton(mActivity.getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create()
                .show();
    }

    public void initGoogleAccount(){
        mCredential = GoogleAccountCredential.usingOAuth2(mActivity, Arrays.asList(new String[]{YouTubeScopes.YOUTUBE_READONLY}))
                .setBackOff(new ExponentialBackOff());

        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            String googleAccountName = mPreferences.getGoogleAccountName();
            if(TextUtils.isEmpty(googleAccountName)){
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }else{
                mCredential.setSelectedAccountName(googleAccountName);
                loadData();
            }
        } else {
            loadData();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(mActivity, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_total_item, parent, false);
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

            if(mDataHashMap.containsKey(itemList.getCid())){
                holder.iv_bookmark.setImageResource(android.R.drawable.btn_star_big_on);
            }else{
                holder.iv_bookmark.setImageResource(android.R.drawable.btn_star_big_off);
            }

            holder.iv_bookmark.setTag(itemList);
            holder.iv_bookmark.setClickable(true);
            holder.iv_bookmark.setOnClickListener(this);
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
                case R.id.iv_bookmark:
                    FirebaseDataItem itemList = (FirebaseDataItem)v.getTag();
                    if(mDataHashMap.containsKey(itemList.getCid())){
                        Log.d(TAG, "kth ViewHolder onClick() mDataHashMap.remove() itemList.getCid() : " + itemList.getCid());
                        mDataHashMap.remove(itemList.getCid());
                    }else{
                        Log.d(TAG, "kth ViewHolder onClick() mDataHashMap.put() itemList.getCid() : " + itemList.getCid());
                        itemList.setBookmarkDate(System.currentTimeMillis());
                        mDataHashMap.put(itemList.getCid(), itemList);
                    }

                    mBookMark.setDataMap(mDataHashMap);
                    mPreferences.setYoutuberBookmark(new Gson().toJson(mBookMark));

                    notifyDataSetChanged();
                    mActivity.refreshFragment(1);
                    break;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_name;
            ImageView iv_bookmark;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                iv_bookmark = (ImageView) itemView.findViewById(R.id.iv_bookmark);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
