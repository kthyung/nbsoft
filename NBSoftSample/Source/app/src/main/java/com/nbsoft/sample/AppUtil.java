package com.nbsoft.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

public class AppUtil {
	private static final String TAG = AppUtil.class.getSimpleName();

	public final static long[] vibratePattern = {0,3};
	private static Vibrator vibrator;

	public static void setWindowFlag(Activity activity, final int bits, boolean on) {
		Window win = activity.getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	public static int convertDpToPixels(float dp, Context context) {
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
		return px;
	}

	public static int convertPixelsToDP(int pixel, Context context) {
		float dp = 0;
		try {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			dp = pixel / (metrics.densityDpi / 160f);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return (int) dp;
	}


	public static int convertSpToPixels(float sp, Context context) {
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
		return px;
	}

	public static int convertDpToSp(float dp, Context context) {
		int sp = (int) (convertDpToPixels(dp, context) / (float) convertSpToPixels(dp, context));
		return sp;
	}

	public static int getAppVersionCode(Context context){
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void setNewBadgeCount(Context context) {
		try {
			int totalCount = 1;
			setNewBadgeCount(context, totalCount);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void setNewBadgeCount(Context context, int count) {
		Log.d(TAG, "kth setNewBadgeCount() count : " + count);
		try{
			Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
			intent.putExtra("badge_count_package_name", context.getPackageName());
			intent.putExtra("badge_count_class_name", getLauncherClassName(context));

			if(count > 0) {
				intent.putExtra("badge_count", count);
			} else {
				intent.putExtra("badge_count", 0);
			}

			context.sendBroadcast(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static String getLauncherClassName(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setPackage(context.getPackageName());

		List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
		if (resolveInfoList != null && resolveInfoList.size() > 0) {
			return resolveInfoList.get(0).activityInfo.name;
		} else {
			return "";
		}
	}

	public final static int NEW_VER = -1;
	public final static int EQAL_VER = 0;
	public final static int OLD_VER = 1;
	public static int compareVersion(String current_ver, String latest_ver) throws Exception {
		int[] A = getVersion(current_ver);
		int[] B = getVersion(latest_ver);

		int i = 0;
		Log.d(TAG, "kth compareVersion(), current_ver A : " + current_ver + ", latest_ver : " + latest_ver);

		for (; i < A.length && i < B.length; i++) {
			if (A[i] > B[i])
				return OLD_VER;
			else if (A[i] < B[i])
				return NEW_VER;
		}
		if (A.length > B.length) {
			for (; i < A.length; i++) {
				if (A[i] > 0) {
					return OLD_VER;
				}
			}
		} else if (A.length < B.length) {
			for (; i < B.length; i++) {
				if (B[i] > 0) {
					return NEW_VER;
				}
			}
		}

		return EQAL_VER;
	}

	public static int[] getVersion(String s) throws Exception {
		String[] S = s.split("\\.");
		int[] N = new int[S.length];
		for (int i = 0; i < S.length; i++) {
			Log.d(TAG, "kth getVersion[" + i + "] " + S[i]);
			N[i] = Integer.parseInt(S[i]);
		}

		return N;
	}

	public static void goToURL(Context context, String url) {
		try {
			Log.d(TAG, "kth goToURL() url : " + url);

			String link = url;
			if (!link.startsWith("http")) {
				link = "http://" + url;
			}

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse(link));
			context.startActivity(intent);
		}
		catch (Exception e) { e.printStackTrace();
		}
	}

	public static void showSoftKeyboard(Context context, View view) {
		try {
			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(view, 0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void hideSoftKeyboard(Context context, View view) {
		try {
			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}