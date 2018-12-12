package com.nbsoft.tvofall.activity.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.R;
import com.nbsoft.tvofall.activity.AppInfoActivity;

public class AppInfoRecommandFragment extends Fragment {
    public static final String TAG = AppInfoRecommandFragment.class.getSimpleName();

    private AppInfoActivity mActivity;
    private AppPreferences mPreferences;

    private Button btn_rate_ok, btn_recommand_ok;

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
                case R.id.btn_rate_ok: {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    try {
                        intent.setData(Uri.parse("market://details?id=" + mActivity.getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + mActivity.getPackageName()));
                        startActivity(intent);
                    }
                }
                    break;
                case R.id.btn_recommand_ok: {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + mActivity.getPackageName());
                    startActivity(Intent.createChooser(intent, mActivity.getString(R.string.popup_recommand_title)));
                }
                    break;
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
        View view =  inflater.inflate(R.layout.fragment_appinfo_recommand, container, false);

        refreshListView(view);

        return view;
    }

    public void refreshListView(){
        refreshListView(getView());
    }

    public void refreshListView(View view){
        btn_rate_ok = (Button)view.findViewById(R.id.btn_rate_ok);
        btn_rate_ok.setOnClickListener(onClickListener);

        btn_recommand_ok = (Button)view.findViewById(R.id.btn_recommand_ok);
        btn_recommand_ok.setOnClickListener(onClickListener);
    }
}
