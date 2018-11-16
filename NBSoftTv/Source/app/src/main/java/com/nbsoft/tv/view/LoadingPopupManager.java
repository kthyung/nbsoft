package com.nbsoft.tv.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LoadingPopupManager {
    public static final String TAG = LoadingPopupManager.class.getSimpleName();

    private volatile static LoadingPopupManager sInstance;

    private Context mContext;
    private Handler mHandler;
    private LoadingPopupWindow mLoading = null;

    public interface LoadingPopupDismissListener{
        void dismiss();
    };

    private LoadingPopupManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static LoadingPopupManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LoadingPopupManager.class) {
                if (sInstance == null) {
                    sInstance = new LoadingPopupManager(context);
                }
            }
        }
        return sInstance;
    }


    /**
     * 로딩을 시작한다.
     *
     * @param activity
     * @param cancelable
     * @param f
     */
    public void showLoading(final Activity activity, final boolean cancelable, String f) {
        Log.d(TAG, "[" + f + "] showLoading()");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _showLoading(activity, cancelable);
            }
        });
    }

    /**
     * 로딩을 시작한다.
     *
     * @param activity
     * @param cancelable
     * @param cancelable
     * @param f
     */
    public void showLoading(final Activity activity, final boolean cancelable, final int backgroundType, String f) {
        Log.d(TAG, "[" + f + "] showLoading()");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _showLoading(activity, cancelable, backgroundType);
            }
        });
    }

    /**
     * 로딩을 시작한다.
     *
     * @param activity
     * @param cancelable
     * @param listener
     * @param f
     */
    public void showLoading(final Activity activity, final boolean cancelable,
                            final LoadingPopupManager.LoadingPopupDismissListener listener, String f) {
        Log.d(TAG, "[" + f + "] showLoading()");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _showLoading(activity, cancelable, listener);
            }
        });
    }

    /**
     * 로딩을 시작한다.
     *
     * @param activity
     * @param cancelable
     * @param listener
     * @param backgroundType
     * @param f
     */
    public void showLoading(final Activity activity, final boolean cancelable, final int backgroundType,
                            final LoadingPopupManager.LoadingPopupDismissListener listener, String f) {
        Log.d(TAG, "[" + f + "] showLoading()");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _showLoading(activity, cancelable, backgroundType, listener);
            }
        });
    }

    private synchronized void _showLoading(Activity activity, boolean cancelable) {
        if (mLoading != null) {
            return;
        }
        mLoading = new LoadingPopupWindow(mContext, activity.getWindow().getDecorView(), cancelable);
        mLoading.show();
    }

    private synchronized void _showLoading(Activity activity, boolean cancelable, int backgroudType) {
        if (mLoading != null) {
            return;
        }
        mLoading = new LoadingPopupWindow(mContext, activity.getWindow().getDecorView(), cancelable, backgroudType);
        mLoading.show();
    }

    private synchronized void _showLoading(Activity activity, boolean cancelable,
                                           final LoadingPopupManager.LoadingPopupDismissListener listener) {
        if (mLoading != null) {
            return;
        }
        mLoading = new LoadingPopupWindow(mContext, activity.getWindow().getDecorView(), cancelable, listener);
        mLoading.show();
    }

    private synchronized void _showLoading(Activity activity, boolean cancelable, int backgroudType,
                                           final LoadingPopupManager.LoadingPopupDismissListener listener) {
        if (mLoading != null) {
            return;
        }
        mLoading = new LoadingPopupWindow(mContext, activity.getWindow().getDecorView(), cancelable, backgroudType, listener);
        mLoading.show();
    }

    /**
     * 로딩을 종료한다.
     *
     * @param f
     */
    public void hideLoading(String f) {
        Log.d(TAG, "[" + f + "] hideLoading()");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _hideLoading();
            }
        });
    }

    private synchronized void _hideLoading() {
        if (mLoading != null) {
            mLoading.dismiss();
            mLoading = null;
        }
    }
}
