package com.nbsoft.sample.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.nbsoft.sample.Define;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class VolleyRequestQueue {
    private static VolleyRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private VolleyRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized VolleyRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());

            //실제 개발용 개인키를 통해 인증하려고할때 사용
            //mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), new HurlStack(null, newSslSocketFactory() ));
        }
        return mRequestQueue;
    }


    /*private SSLSocketFactory newSslSocketFactory()
    {
        try
        {

            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("BKS");
            // Get the raw resource, which contains the keystore with // your trusted certificates (root and any intermediate certs)
            InputStream in = mCtx.getApplicationContext().getResources().openRawResource(R.raw.campserver);
            try
            {
                // Initialize the keystore with the provided trusted certificates
                // Provide the password of the keystore
                //trusted.load(in, KEYSTORE_PASSWORD);
                trusted.load(in, Define.KEYSTORE_PASSWORD.toCharArray());
            }
            finally
            {
                in.close();
            }

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trusted);
            SSLContext context = SSLContext.getInstance("TLS");

            context.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory sf = context.getSocketFactory();
            return sf;
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }*/



    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void clearQueue(){
        clearQueue();
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
