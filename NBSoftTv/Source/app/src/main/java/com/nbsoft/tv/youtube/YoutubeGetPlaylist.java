package com.nbsoft.tv.youtube;

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
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.R;
import com.nbsoft.tv.activity.YoutuberPlaylistActivity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class YoutubeGetPlaylist{
    public static final String TAG = YoutuberPlaylistActivity.class.getSimpleName();

    private Context mContext;

    private String mCid;
    private String mPageToken;

    private GoogleAccountCredential mCredential;

    private AppPreferences mPreferences;

    private YoutubeGetPlaylistListener mListener;

    public YoutubeGetPlaylist(Context context){
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

    public void getYoutubePlaylist(String cid, String pageToken, YoutubeGetPlaylistListener listener){
        mCid = cid;
        mPageToken = pageToken;
        mListener = listener;

        new YoutubePlaylistRequestTask().execute();
    }

    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class YoutubePlaylistRequestTask extends AsyncTask<Void, Void, PlaylistListResponse> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        public YoutubePlaylistRequestTask() {
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
        protected PlaylistListResponse doInBackground(Void... params) {
            Log.d(TAG, "kth YoutubePlaylistRequestTask doInBackground() mCid : " + mCid);
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
        private PlaylistListResponse getDataFromApi() throws IOException {
            PlaylistListResponse response = mService.playlists().list("snippet,contentDetails")
                    .setChannelId(mCid)
                    .setPageToken(mPageToken)
                    .setMaxResults(10L)
                    .execute();

            if(response != null){
                //Log.d(TAG, "kth YoutubePlaylistRequestTask getDataFromApi() response.toString() : " + response.toString());
            }

            return response;
        }


        @Override
        protected void onPreExecute() { }

        @Override
        protected void onPostExecute(PlaylistListResponse response) {
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
                } else {
                    mListener.onFail(mLastError);
                }
            }
        }
    }

    public interface YoutubeGetPlaylistListener{
        public void onSuccess(List<Playlist> resultList, String pageToken);
        public void onFail(Exception e);
        public void onAuthFail(Exception e);
    }
}
