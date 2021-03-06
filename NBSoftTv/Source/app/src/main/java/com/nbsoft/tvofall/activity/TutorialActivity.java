package com.nbsoft.tvofall.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.R;

public class TutorialActivity extends AppCompatActivity {
    public static final String TAG = TutorialActivity.class.getSimpleName();

    private Context mContext;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setDimAmount(0.8f);
        getWindow().setGravity(Gravity.CENTER);
        setContentView(R.layout.activity_tutorial);

        mContext = this;
        mPreferences = new AppPreferences(mContext);

        initLayout();
    }

    private void initLayout(){

    }

}
