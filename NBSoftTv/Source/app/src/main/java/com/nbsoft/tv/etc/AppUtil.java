package com.nbsoft.tv.etc;

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
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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

	public static String getMarketVersion(Context context) {
		String mVer = "";
		try {
			Document doc = Jsoup
					.connect("https://play.google.com/store/apps/details?id=com.dailylife.communication")
					.get();

			Elements elements = doc.select(".htlgb ");
			for (int i=0; i<elements.size(); i++) {
				mVer = elements.get(i).text();
				if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", mVer)) {
					break;
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}

		Log.d(TAG, "kth getMarketVersion() mVer : " + mVer);
		return mVer;
	}

	public static String getApplicationVersion(Context context) {
		String version = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			version = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "kth getApplicationVersion() version : " + version);
		return version;
	}

	public static int checkVersion(String strDeviceVersion, String strServerVersion) {
		int nDV1, nDV2, nDV3;
		int nSV1, nSV2, nSV3;

		nDV1 = Integer.parseInt(strDeviceVersion.split("\\.")[0]);
		nDV2 = Integer.parseInt(strDeviceVersion.split("\\.")[1]);
		nDV3 = Integer.parseInt(strDeviceVersion.split("\\.")[2]);

		nSV1 = Integer.parseInt(strServerVersion.split("\\.")[0]);
		nSV2 = Integer.parseInt(strServerVersion.split("\\.")[1]);
		nSV3 = Integer.parseInt(strServerVersion.split("\\.")[2]);

		int result = 0;
		if (nSV1 > nDV1) {
			result = 1;
		} else {
			if (nSV1 == nDV1 && nSV2 > nDV2) {
				result = 1;
			} else {
				if (nSV1 == nDV1 && nSV2 == nDV2 && nSV3 > nDV3) {
					result = 2;
				} else {
					result = 0;
				}
			}
		}

		Log.d(TAG, "kth checkVersion() result : " + result);
		return result;
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

	public static String getTypeToString(int type){
		String returnStr = "";
		switch(type){
			case 0:
				returnStr = "s";
				break;
			case 1:
				returnStr = "g";
				break;
			case 2:
				returnStr = "m";
				break;
			case 3:
				returnStr = "c";
				break;
			case 4:
				returnStr = "b";
				break;
			case 5:
				returnStr = "k";
				break;
			case 6:
				returnStr = "o";
				break;
			default:
				returnStr = "";
				break;
		}

		return returnStr;
	}

	public static String toTimeFromIso8601(final String iso8601string) throws ParseException {
		//1H59M31S
		StringBuilder sb = new StringBuilder();
		String tempStr = iso8601string.replace("PT", "");
		int hIndex = tempStr.lastIndexOf("H");
		int mIndex = tempStr.lastIndexOf("M");
		int sIndex = tempStr.lastIndexOf("S");

		String hourStr = "";
		String minuteStr = "";
		String secondStr = "";
		if(hIndex != -1){
			hourStr = tempStr.substring(0, hIndex);
			minuteStr = tempStr.substring(hIndex+1, mIndex);
			secondStr = tempStr.substring(mIndex+1, sIndex);

			sb.append(hourStr);
			sb.append(":");
			sb.append(minuteStr);
			sb.append(":");
			sb.append(secondStr);
		}else{
			minuteStr = tempStr.substring(0, mIndex);
			secondStr = tempStr.substring(mIndex+1, sIndex);

			sb.append(minuteStr);
			sb.append(":");
			sb.append(secondStr);
		}

		return sb.toString();
	}
}