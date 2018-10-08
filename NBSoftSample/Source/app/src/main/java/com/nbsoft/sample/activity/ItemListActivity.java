package com.nbsoft.sample.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsoft.sample.AppUtil;
import com.nbsoft.sample.GlideApp;
import com.nbsoft.sample.R;
import com.nbsoft.sample.model.DataItemList;

import java.net.URI;
import java.util.ArrayList;

public class ItemListActivity extends AppCompatActivity {
    public static final String TAG = ItemListActivity.class.getSimpleName();

    private Context mContext;

    private long backKeyPressedTime = 0;
    private Toast toast_appclose = null;

    private Toolbar toolbar;

    private DrawerLayout mDrawerLayout;

    private TextView tv_toolbar_title;
    private RelativeLayout rl_toolbar_drawer;

    private ArrayList<DataItemList> dataArrayList;

    private RecyclerView rv_contents;
    private LinearLayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_toolbar_drawer:
                    if (mDrawerLayout != null) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (Build.VERSION.SDK_INT >= 21) {
            AppUtil.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(getResources().getColor(R.color.app_primary_dark));
        }

        mContext = this;

        initLayout();
        loadList();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            if (backKeyPressedTime + 2000 < System.currentTimeMillis()) {
                backKeyPressedTime = System.currentTimeMillis();
                toast_appclose = Toast.makeText(mContext, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast_appclose.show();
                return;
            } else if (backKeyPressedTime + 2000 >= System.currentTimeMillis()) {
                super.onBackPressed();
                if (toast_appclose != null)
                    toast_appclose.cancel();
            }
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
        tv_toolbar_title.setText("Item List");

        rl_toolbar_drawer = (RelativeLayout) findViewById(R.id.rl_toolbar_drawer);
        rl_toolbar_drawer.setClickable(true);
        rl_toolbar_drawer.setOnClickListener(onClickListener);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.container);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "kth onDrawerOpened()");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "kth onDrawerClosed()");


            }
        };
        mDrawerLayout.addDrawerListener(toggle);

        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);
    }

    private void loadList(){
        dataArrayList = new ArrayList<DataItemList>();
        for(int i=0; i<30; i++){
            dataArrayList.add(new DataItemList(i, "name"+i, "content"+i,
                    ""));
        }

        rv_contents = (RecyclerView) findViewById(R.id.rv_contents);

        mLayoutManager = new LinearLayoutManager(mContext);

        rv_contents.setLayoutManager(mLayoutManager);
        rv_contents.setItemAnimator(new DefaultItemAnimator());
        rv_contents.setNestedScrollingEnabled(false);
        rv_contents.setHasFixedSize(false);

        mAdapter = new RecyclerAdapter(mContext, dataArrayList);

        rv_contents.setAdapter(mAdapter);
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener{
        Context context;
        ArrayList<DataItemList> dataArrayList;

        public RecyclerAdapter(Context context, ArrayList<DataItemList> dataArrayList) {
            this.context = context;
            this.dataArrayList = dataArrayList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return this.dataArrayList.size();
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.ViewHolder holder, int position) {
            DataItemList itemList = dataArrayList.get(position);

            GlideApp.with(context)
                    .load(R.mipmap.ic_launcher)
                    .into(holder.iv_content);

            holder.tv_name.setText(itemList.getName());
            holder.tv_description.setText(itemList.getContent());

            holder.rl_main_layout.setTag(itemList.getId());
            holder.rl_main_layout.setClickable(true);
            holder.rl_main_layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "kth ViewHolder onClick()");
            switch(v.getId()){
                case R.id.rl_main_layout:
                    Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("itemId", (int)v.getTag());
                    startActivity(intent);
                    break;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            RecyclerAdapter.ViewHolder holder;

            RelativeLayout rl_main_layout;
            ImageView iv_content;
            TextView tv_name, tv_description;

            int position;

            public ViewHolder(View itemView) {
                super(itemView);

                rl_main_layout = (RelativeLayout) itemView.findViewById(R.id.rl_main_layout);
                iv_content = (ImageView) itemView.findViewById(R.id.iv_content);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_description = (TextView) itemView.findViewById(R.id.tv_description);
            }

            public void bind(int position)
            {
                this.position = position;
            }
        }
    }
}
