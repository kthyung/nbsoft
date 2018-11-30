package com.nbsoft.tv.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;
import com.nbsoft.tv.etc.AppUtil;
import com.nbsoft.tv.model.FirebaseRequestItem;
import com.nbsoft.tv.model.YoutuberRequest;
import com.nbsoft.tv.view.LoadingPopupManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class YoutuberRequestActivity extends AppCompatActivity {
    public static final String TAG = YoutuberRequestActivity.class.getSimpleName();

    private Context mContext;
    private AppPreferences mPreferences;
    private DatabaseReference mDatabase;

    private List<FirebaseRequestItem> mArrDataList;

    private YoutuberRequest mRequest = new YoutuberRequest();
    private List<FirebaseRequestItem> dataArray = new ArrayList<FirebaseRequestItem>();

    private RecyclerView rv_contents;
    private LinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private ImageView iv_toolbar_left, iv_toolbar_right;
    private RelativeLayout rl_toolbar_left, rl_toolbar_right;

    private TextView tv_text;
    private EditText dt_input;
    private Button btn_input;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_toolbar_left:
                    finish();
                    break;
                case R.id.btn_input:
                    requestInputText();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtuber_request);

        mContext = this;
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
        tv_toolbar_title.setText(mContext.getString(R.string.title_request));

        iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
        iv_toolbar_left.setImageResource(R.drawable.btn_title_befor_nor);
        rl_toolbar_left = (RelativeLayout) findViewById(R.id.rl_toolbar_left);
        rl_toolbar_left.setClickable(true);
        rl_toolbar_left.setOnClickListener(onClickListener);
        rl_toolbar_left.setVisibility(View.VISIBLE);

        iv_toolbar_right = (ImageView) findViewById(R.id.iv_toolbar_right);
        iv_toolbar_right.setImageResource(R.drawable.btn_title_option_nor);
        rl_toolbar_right = (RelativeLayout) findViewById(R.id.rl_toolbar_right);
        rl_toolbar_right.setClickable(false);
        rl_toolbar_right.setOnClickListener(null);
        rl_toolbar_right.setVisibility(View.INVISIBLE);

        tv_text = (TextView) findViewById(R.id.tv_text);
        dt_input = (EditText) findViewById(R.id.dt_input);

        btn_input = (Button) findViewById(R.id.btn_input);
        btn_input.setClickable(true);
        btn_input.setOnClickListener(onClickListener);

        dt_input.clearFocus();
        AppUtil.hideSoftKeyboard(mContext, dt_input);
    }

    private void loadData(){
        mArrDataList = new ArrayList<FirebaseRequestItem>();

        String youtuberRequest = mPreferences.getYoutuberRequest();
        if(!TextUtils.isEmpty(youtuberRequest)){
            mRequest = new Gson().fromJson(youtuberRequest, YoutuberRequest.class);
            if(mRequest!=null){
                dataArray = mRequest.getDataMap();
                mArrDataList.addAll(dataArray);
            }
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

    private void requestInputText(){
        int requestCount = mPreferences.getRequestCount();
        long requestTime = mPreferences.getLastRequestTime();

        Calendar requestCalendar = Calendar.getInstance();
        requestCalendar.setTimeInMillis(requestTime);
        int requestDay = requestCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar nowCalendar = Calendar.getInstance();
        int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
        if(requestDay != nowDay){
            //0으로 초기화
            requestCount = 0;
            mPreferences.setRequestCount(0);
        }


        if(requestCount >= 5){
            Toast.makeText(mContext, mContext.getString(R.string.request_error_count), Toast.LENGTH_SHORT).show();
            return;
        }

        String name = dt_input.getText().toString();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(mContext, mContext.getString(R.string.request_error_input), Toast.LENGTH_SHORT).show();
        }else if(name.length() == 1){
            Toast.makeText(mContext, mContext.getString(R.string.request_error_input), Toast.LENGTH_SHORT).show();
        }else{
            String key = mDatabase.child("request").push().getKey();
            if(key != null){
                LoadingPopupManager.getInstance(mContext).showLoading(YoutuberRequestActivity.this, true, "YoutuberRequestActivity");

                final FirebaseRequestItem item = new FirebaseRequestItem();
                item.setDate(new SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN).format(new Date(System.currentTimeMillis())));
                item.setCName(name);
                item.setAccount(mPreferences.getGoogleAccountName());

                mDatabase.child("request").child(key).setValue(item)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "kth requestInputText() onSuccess() ");
                                mPreferences.setRequestCount(mPreferences.getRequestCount()+1);
                                mPreferences.setLastRequestTime(System.currentTimeMillis());

                                Toast.makeText(mContext, mContext.getString(R.string.request_success), Toast.LENGTH_SHORT).show();
                                dt_input.setText("");

                                dataArray.add(0, item);
                                mRequest.setDataMap(dataArray);
                                mPreferences.setYoutuberRequest(new Gson().toJson(mRequest));

                                loadData();
                                refreshListView();

                                LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberRequestActivity");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "kth requestInputText() onFailure()");
                                e.printStackTrace();

                                Toast.makeText(mContext, mContext.getString(R.string.request_error_fail), Toast.LENGTH_SHORT).show();

                                LoadingPopupManager.getInstance(mContext).hideLoading("YoutuberRequestActivity");
                            }
                        });
            }
        }
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_CONTENT = 1;

        Context context;
        List<FirebaseRequestItem> dataArrayList;

        public RecyclerAdapter(Context context, List<FirebaseRequestItem> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        public void setData(List<FirebaseRequestItem> dataArrayList){
            this.dataArrayList = dataArrayList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_HEADER){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_request_header, parent, false);
                return new RecyclerAdapter.HeaderViewHolder(v);
            }else if(viewType == TYPE_CONTENT){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_youtuber_request_item, parent, false);
                return new RecyclerAdapter.ViewHolder(v);
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0){
                return TYPE_HEADER;
            }else{
                return TYPE_CONTENT;
            }
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size()+1;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof HeaderViewHolder){
                HeaderViewHolder headerViewHolder = (HeaderViewHolder)holder;

                headerViewHolder.tv_num.setText(context.getString(R.string.request_num));
                headerViewHolder.tv_name.setText(context.getString(R.string.request_name));
                headerViewHolder.tv_date.setText(context.getString(R.string.request_date));
                headerViewHolder.tv_status.setText(context.getString(R.string.request_status));

                headerViewHolder.rl_main_layout.setClickable(false);
                headerViewHolder.rl_main_layout.setOnClickListener(null);
            }else if(holder instanceof ViewHolder){
                FirebaseRequestItem itemList = dataArrayList.get(position-1);

                ViewHolder contentHolder = (ViewHolder)holder;

                contentHolder.tv_num.setText(String.valueOf(position));
                contentHolder.tv_name.setText(itemList.getCName());
                contentHolder.tv_date.setText(itemList.getDate());
                contentHolder.tv_status.setText("");

                contentHolder.rl_main_layout.setClickable(false);
                contentHolder.rl_main_layout.setOnClickListener(null);
            }
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout :

                    break;
            }
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            LinearLayout rl_main_layout;
            TextView tv_num;
            TextView tv_name;
            TextView tv_date;
            TextView tv_status;

            int position;

            public HeaderViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (LinearLayout) itemView.findViewById(R.id.rl_main_layout);
                tv_num = (TextView) itemView.findViewById(R.id.tv_num);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
                tv_status = (TextView) itemView.findViewById(R.id.tv_status);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RecyclerAdapter.ViewHolder holder;

            LinearLayout rl_main_layout;
            TextView tv_num;
            TextView tv_name;
            TextView tv_date;
            TextView tv_status;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (LinearLayout) itemView.findViewById(R.id.rl_main_layout);
                tv_num = (TextView) itemView.findViewById(R.id.tv_num);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
                tv_status = (TextView) itemView.findViewById(R.id.tv_status);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
