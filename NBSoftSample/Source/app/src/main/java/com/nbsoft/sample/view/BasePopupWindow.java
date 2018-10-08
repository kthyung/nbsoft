package com.nbsoft.sample.view;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

public abstract class BasePopupWindow {

	protected Context mContext;
	protected View mParent;
	protected View mLayout;

	protected PopupWindow mPopupWindow;
	
	public BasePopupWindow(Context context, View parent) {
		this.mContext = context;
		this.mParent  = parent;
	}
	
	protected void onCreate(Context context, int layoutResId) {
		onCreate(context, layoutResId, false);
	}

	protected void onCreate(Context context, int layoutResId, boolean isCancelable) {
		onCreate(context, layoutResId, isCancelable, android.R.style.Animation_Dialog);
	}

	protected void onCreate(Context context, int layoutResId, boolean isCancelable, int animationStyle) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mLayout = inflater.inflate(layoutResId, null);
		mPopupWindow = new PopupWindow(mLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		if (isCancelable) { 
			mPopupWindow.setOutsideTouchable(isCancelable);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)) ;
		}

		if (animationStyle != 0) {
			mPopupWindow.setAnimationStyle(animationStyle);
		}
	}

	protected View findViewById(int id) {
		return mLayout.findViewById(id);
	}
	
	public void show() {
		try {
			if (mPopupWindow == null){
				return;
			}

			if (mParent == null) {
				dismiss();
				return;
			}

			mPopupWindow.setAnimationStyle(0);
			mPopupWindow.showAtLocation(mParent, Gravity.CENTER, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dismiss() {
		try {
			if (mPopupWindow == null){
				return;
			}
			
			mPopupWindow.dismiss();
			onClose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isShowing(){
		if (mPopupWindow == null){
			return false; 
		}

		return mPopupWindow.isShowing();
	}

	protected void onClose() {

	}
	
	public void setSoftInputMode(int mode){
		if(mode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE ||
				mode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN){
			mPopupWindow.setSoftInputMode(mode);
		}
	}
}
