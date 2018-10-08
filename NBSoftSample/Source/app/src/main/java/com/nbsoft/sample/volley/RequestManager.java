package com.nbsoft.sample.volley;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.nbsoft.sample.AppPreferences;
import com.nbsoft.sample.Define;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestManager {
    private final String TAG = RequestManager.class.getSimpleName();
    private static RequestManager instance;
    private String protocol;
    private String host;
    private String port;
    private AppPreferences prefs;
    private String baseUrl;

    public static RequestManager getInstance() {
        if(instance == null) {
            synchronized (RequestManager.class) {
                instance = new RequestManager();
            }
        }
        return instance;
    }

    public void inititalize(Context context) {
        protocol = Define.PROTOCOL_HTTP;
        host = Define.SERVER_IP;
        port = Define.SERVER_PORT_HTTP;
        baseUrl = protocol + host + ":" + port;

        prefs = new AppPreferences(context);
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
}
