
package com.nbsoft.tv.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nbsoft.tv.R;

public class LoadingPopupWindow extends BasePopupWindow {
	private LinearLayout main_layout;
	private TextView tv_desc;
	private View vw_padding_top;
	private View vw_padding_bottom;
	private ImageView iv_loading;
	private Animation mAnim = null;
	private boolean mNormalDismiss = false;
	private boolean mUplaod = false;
	
	private AnimationDrawable mLoadingAni=null;
	public LoadingPopupWindow(Context context, View parent, boolean cancelable) {
		super(context, parent);
		// 
		onCreate(context, R.layout.popup_loading_dynamic, cancelable, 0);

		main_layout       = (LinearLayout)findViewById(R.id.main_layout);
		tv_desc           = (TextView)findViewById(R.id.tv_desc);
		vw_padding_top    = (View)findViewById(R.id.vw_padding_top);
		vw_padding_bottom = (View)findViewById(R.id.vw_padding_bottom);
		iv_loading		  = (ImageView)findViewById(R.id.iv_loading);
		//mLoadingAni=(AnimationDrawable)ID_LOADING.getBackground();
		tv_desc          .setVisibility(View.GONE);
		vw_padding_top   .setVisibility(View.GONE);
		vw_padding_bottom.setVisibility(View.GONE);
		
		mAnim = AnimationUtils.loadAnimation(context, R.anim.loading_motion);
	}

    public LoadingPopupWindow(Context context, View parent, boolean cancelable, int backgroundType) {
        super(context, parent);
        //
        onCreate(context, R.layout.popup_loading_dynamic, cancelable, 0);

        main_layout       = (LinearLayout)findViewById(R.id.main_layout);
        tv_desc           = (TextView)findViewById(R.id.tv_desc);
        vw_padding_top    = (View)findViewById(R.id.vw_padding_top);
        vw_padding_bottom = (View)findViewById(R.id.vw_padding_bottom);
        iv_loading		  = (ImageView)findViewById(R.id.iv_loading);
        //mLoadingAni=(AnimationDrawable)ID_LOADING.getBackground();
        tv_desc          .setVisibility(View.GONE);
        vw_padding_top   .setVisibility(View.GONE);
        vw_padding_bottom.setVisibility(View.GONE);

        mAnim = AnimationUtils.loadAnimation(context, R.anim.loading_motion);

        if(backgroundType == 1){
            main_layout.setBackgroundColor(0x33000000);
        }
    }

    public LoadingPopupWindow(Context context, View parent, boolean cancelable,
                              final LoadingPopupManager.LoadingPopupDismissListener listener) {
        super(context, parent);
        //
        onCreate(context, R.layout.popup_loading_dynamic, cancelable, 0);

        main_layout       = (LinearLayout)findViewById(R.id.main_layout);
        tv_desc           = (TextView)findViewById(R.id.tv_desc);
        vw_padding_top    = (View)findViewById(R.id.vw_padding_top);
        vw_padding_bottom = (View)findViewById(R.id.vw_padding_bottom);
        iv_loading		  = (ImageView)findViewById(R.id.iv_loading);
        //mLoadingAni=(AnimationDrawable)ID_LOADING.getBackground();
        tv_desc          .setVisibility(View.GONE);
        vw_padding_top   .setVisibility(View.GONE);
        vw_padding_bottom.setVisibility(View.GONE);

        mAnim = AnimationUtils.loadAnimation(context, R.anim.loading_motion);
        mUplaod = true;
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(mUplaod && !mNormalDismiss) {
                    if (listener != null) {
                        listener.dismiss();
                    }
                }
                mNormalDismiss = false;
                mUplaod = false;
            }
        });
    }

    public LoadingPopupWindow(Context context, View parent, boolean cancelable, int backgroundType,
                              final LoadingPopupManager.LoadingPopupDismissListener listener) {
        super(context, parent);

        onCreate(context, R.layout.popup_loading_dynamic, cancelable, 0);

        main_layout       = (LinearLayout)findViewById(R.id.main_layout);
        tv_desc           = (TextView)findViewById(R.id.tv_desc);
        vw_padding_top    = (View)findViewById(R.id.vw_padding_top);
        vw_padding_bottom = (View)findViewById(R.id.vw_padding_bottom);
        iv_loading		  = (ImageView)findViewById(R.id.iv_loading);
        //mLoadingAni=(AnimationDrawable)ID_LOADING.getBackground();
        tv_desc          .setVisibility(View.GONE);
        vw_padding_top   .setVisibility(View.GONE);
        vw_padding_bottom.setVisibility(View.GONE);

        mAnim = AnimationUtils.loadAnimation(context, R.anim.loading_motion);

        if(backgroundType == 1){
            main_layout.setBackgroundColor(0x33000000);
        }

        mUplaod = true;
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(mUplaod && !mNormalDismiss) {
                    if (listener != null) {
                        listener.dismiss();
                    }
                }
                mNormalDismiss = false;
                mUplaod = false;
            }
        });
    }
	
	public void setText(String text) {
		tv_desc.setText(text);
		tv_desc.setVisibility(View.VISIBLE);
	}
	
	public void setPadding(boolean padding) {
		if (padding) {
			vw_padding_top   .setVisibility(View.VISIBLE);
			vw_padding_bottom.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void show() {
		//Log.d("LoadingPopupWindow", "Popup Show" );
//		loadAnimation();
		start(); 
		super.show();
	}

	@Override
	public void dismiss() {
		//Log.d("LoadingPopupWindow", "Popup hide" );
		stop();
        mNormalDismiss = true;
		super.dismiss();
	}
	
	public void start(){
		//mLoadingAni.start();
		iv_loading.setAnimation(mAnim);
		iv_loading.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		iv_loading.startAnimation(mAnim);
	}
	 
	public void stop(){
		iv_loading.clearAnimation();
		//mLoadingAni.stop();
	}
}
