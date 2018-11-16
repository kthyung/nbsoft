package com.nbsoft.tv.volley;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.nbsoft.tv.AppPreferences;
import com.nbsoft.tv.Define;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {
    private final String TAG = RequestManager.class.getSimpleName();
    private static RequestManager instance;
    private String protocol;
    private String host;
    private String port;
    private AppPreferences prefs;
    private String baseUrl;

    public static RequestManager getInstance(Context context) {
        if(instance == null) {
            synchronized (RequestManager.class) {
                instance = new RequestManager(context);
            }
        }
        return instance;
    }

    public RequestManager(){

    }

    public RequestManager(Context context){
        protocol = Define.PROTOCOL_HTTP;
        host = Define.SERVER_IP;
        port = Define.SERVER_PORT_HTTP;
        baseUrl = protocol + host + ":" + port;

        prefs = new AppPreferences(context);
    }

    public void inititalizeYoutube() {
        protocol = Define.PROTOCOL_YOUTUBE_HTTP;
        host = Define.SERVER_YOUTUBE_IP;
        port = Define.SERVER_YOUTUBE_PORT;
        baseUrl = protocol + host;
        if(!TextUtils.isEmpty(port)){
            baseUrl += ":" + port;
        }
    }

    public void requestListItem(Context context, int startIndex, int count, ResponseListener listener) {
        String command = "/list";
        Map<String, String> headers = new HashMap<String, String>();

        Map<String, String> bodys = new HashMap<String, String>();
        JSONObject body = new JSONObject();
        try {
            body.put("startIndex", startIndex);
            body.put("count", count);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "kth requestListItem() jSON Body Exception: " + e.toString());
        }

        bodys.put("body", body.toString());

        String url = baseUrl + command;
        VolleySendRequest.send(context, Request.Method.GET, url, headers, bodys, listener);
    }

    /*public void requestGoogleAuth(Context context, ResponseListener listener) {
        Map<String, String> headers = new HashMap<String, String>();

        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", context.getString(R.string.oauth_client_id));
        params.put("redirect_uri", "http://localhost:port");
        params.put("response_type", "code");
        params.put("scope", "https://www.googleapis.com/auth/youtube");

        Map<String, String> bodys = new HashMap<String, String>();
        JSONObject body = new JSONObject();

        bodys.put("body", body.toString());

        String url = "https://accounts.google.com/o/oauth2/auth";
        VolleySendRequest.send(context, Request.Method.POST, url, headers, params, bodys, listener);
    }*/

    public void requestYoutuberPlaylist(Context context, String cid, ResponseListener listener) {
        String command = "/playlists";
        Map<String, String> headers = new HashMap<String, String>();

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", GoogleSignIn.getLastSignedInAccount(context).getIdToken());
        params.put("part", "snippet");
        params.put("fields", "items(snippet)");
        params.put("channelId", cid);

        Map<String, String> bodys = new HashMap<String, String>();
        JSONObject body = new JSONObject();

        bodys.put("body", body.toString());

        String url = baseUrl + command;
        VolleySendRequest.send(context, Request.Method.GET, url, headers, params, bodys, listener);
    }

    public void requestYoutuberPlaylistItem(Context context, String playlistId, ResponseListener listener) {
        String command = "/playlistItems";
        Map<String, String> headers = new HashMap<String, String>();

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", GoogleSignIn.getLastSignedInAccount(context).getIdToken());
        params.put("part", "snippet");
        params.put("fields", "items(snippet)");
        params.put("playlistId", playlistId);

        Map<String, String> bodys = new HashMap<String, String>();
        JSONObject body = new JSONObject();

        bodys.put("body", body.toString());

        String url = baseUrl + command;
        VolleySendRequest.send(context, Request.Method.GET, url, headers, params, bodys, listener);
    }
}
