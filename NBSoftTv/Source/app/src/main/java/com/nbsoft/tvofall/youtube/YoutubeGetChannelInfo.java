package com.nbsoft.tvofall.youtube;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.nbsoft.tvofall.AppPreferences;
import com.nbsoft.tvofall.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class YoutubeGetChannelInfo {
    public static final String TAG = YoutubeGetChannelInfo.class.getSimpleName();

    private Context mContext;

    private String mCid;
    private String mPageToken;

    private GoogleAccountCredential mCredential;

    private AppPreferences mPreferences;

    private YoutubeGetChannelInfoListener mListener;

    public YoutubeGetChannelInfo(Context context){
        mContext = context;
        mPreferences = new AppPreferences(mContext);

        initGoogleAccount();
    }

    private void initGoogleAccount(){
        mCredential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(new String[]{YouTubeScopes.YOUTUBE_READONLY}))
                .setBackOff(new ExponentialBackOff());

        if (mCredential.getSelectedAccountName() == null) {
            String googleAccountName = mPreferences.getGoogleAccountName();
            if(TextUtils.isEmpty(googleAccountName)){

            }else{
                mCredential.setSelectedAccountName(googleAccountName);
            }
        }
    }

    public void getYoutubeChannelInfo(String cid, String pageToken, YoutubeGetChannelInfoListener listener){
        mCid = cid;
        mPageToken = pageToken;
        mListener = listener;

        new YoutubeChannelInfoRequestTask().execute();
    }

    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class YoutubeChannelInfoRequestTask extends AsyncTask<Void, Void, ChannelListResponse> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        public YoutubeChannelInfoRequestTask() {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName(mContext.getString(R.string.app_name))
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected ChannelListResponse doInBackground(Void... params) {
            Log.d(TAG, "kth YoutubeChannelInfoRequestTask doInBackground() mCid : " + mCid);
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private ChannelListResponse getDataFromApi() throws IOException {
            ChannelListResponse response = mService.channels().list("snippet,statistics,brandingSettings")
                    .setId(mCid)
                    .setPageToken(mPageToken)
                    .setMaxResults(10L)
                    .execute();

            if(response != null){
                //Log.d(TAG, "kth YoutubeChannelInfoRequestTask getDataFromApi() response.toString() : " + response.toString());
            }

            return response;
        }


        @Override
        protected void onPreExecute() { }

        @Override
        protected void onPostExecute(ChannelListResponse response) {
            if(mListener!=null) {
                if (response != null) {
                    mListener.onSuccess(response.getItems(), response.getNextPageToken());
                }
            }
        }

        @Override
        protected void onCancelled() {
            if(mListener!=null){
                if (mLastError != null) {
                    if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                        mListener.onFail(mLastError);
                    } else if (mLastError instanceof UserRecoverableAuthIOException) {
                        mListener.onAuthFail(mLastError);
                    } else {
                        mListener.onFail(mLastError);
                    }
                }
            }
        }
    }

    public interface YoutubeGetChannelInfoListener{
        public void onSuccess(List<Channel> resultList, String pageToken);
        public void onFail(Exception e);
        public void onAuthFail(Exception e);
    }
}
