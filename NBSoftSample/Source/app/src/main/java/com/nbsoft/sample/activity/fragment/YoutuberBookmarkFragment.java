package com.nbsoft.sample.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nbsoft.sample.R;
import com.nbsoft.sample.model.FirebaseDataItem;

import java.util.List;

public class YoutuberBookmarkFragment extends Fragment {
    public static final String TAG = YoutuberBookmarkFragment.class.getSimpleName();

    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreate()");
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "kth onCreateView()");
        View view =  inflater.inflate(R.layout.fragment_youtuber_bookmark, container, false);

        initLayout();
        return view;
    }

    private void initLayout(){

    }
}
