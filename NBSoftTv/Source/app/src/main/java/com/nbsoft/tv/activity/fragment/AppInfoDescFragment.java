package com.nbsoft.tv.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;
import com.nbsoft.tv.activity.AppInfoActivity;

public class AppInfoDescFragment extends Fragment {
    public static final String TAG = AppInfoDescFragment.class.getSimpleName();

    private AppInfoActivity mActivity;
    private AppPreferences mPreferences;

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

            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreate()");
        super.onCreate(savedInstanceState);

        mActivity = (AppInfoActivity) getActivity();
        mPreferences = new AppPreferences(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreateView()");
        View view =  inflater.inflate(R.layout.fragment_appinfo_desc, container, false);

        refreshListView(view);

        return view;
    }

    public void refreshListView(){
        refreshListView(getView());
    }

    public void refreshListView(View view){

    }
}
