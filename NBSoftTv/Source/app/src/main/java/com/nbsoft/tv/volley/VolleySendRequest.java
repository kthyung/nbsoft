package com.nbsoft.tv.volley;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.nbsoft.tv.etc.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class VolleySendRequest {
    private static final String TAG = "VolleySendRequest";

    /**
     * 헤더 정보만 입력될 경우 사용하는 함수
     * @param context
     * @param requestType
     * @param url
     * @param headers
     * @param listener
     */
    public static void send(final Context context,
                                 int requestType,
                                 String url,
                                 final Map<String, String> headers,
                                 final ResponseListener listener){
        Log.d("AuthApi", "request string >> " + url);

        StringRequest request = new StringRequest(requestType, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(listener != null){
                    listener.OnSuccess(response);
                }else{
                    //Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(listener != null){
                    listener.OnFail(error);
                }else{
                    //Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> h;

                if(headers != null){
                    h = headers;
                }else{
                    h = super.getHeaders();
                }

                return h;
            }
        };
        request.setTag(TAG);
        VolleyRequestQueue.getInstance(context).addToRequestQueue(request);
    }

    /**
     * 헤더 및 body(json string) 정보를 입력할 경우 사용하는 함수
     * @param context
     * @param requestType
     * @param url
     * @param headers
     * @param bodys
     * @param listener
     */
    public static void send(final Context context,
                            int requestType,
                            String url,
                            final Map<String, String> headers,
                            final Map<String, String> bodys,
                            final ResponseListener listener){
        send(context, requestType, url, headers, null, bodys, listener);
    }

    /**
     *헤더, 파라메터, body(json string) 정보를 입력할 경우 사용하는 함수
     * @param context
     * @param requestType
     * @param url
     * @param headers
     * @param params
     * @param bodys
     * @param listener
     */
    public static void send(final Context context,
                            int requestType,
                            String url,
                            final Map<String, String> headers,
                            final Map<String, String> params,
                            final Map<String, String> bodys,
                            final ResponseListener listener){
        Log.d(TAG, "kth send() request string >> " + url);

        String URL = url;
        if(params != null && params.size() > 0) {
            StringBuilder encodedParams = new StringBuilder();
            try {
                int entryCount = 0;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    entryCount++;
                    encodedParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    encodedParams.append('=');
                    encodedParams.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    if (entryCount < params.size()) {
                        encodedParams.append('&');
                    }
                }
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + "UTF-8", uee);
            }

            if(StringUtil.lastStringCheck("/", URL)){
                URL = URL.substring(0, URL.length()-1);
            }

            URL = URL + "?" + encodedParams.toString();

            Log.d(TAG, "kth send() request url >> " + URL);
        }

        JsonBodyRequest jsonRequest = new JsonBodyRequest(requestType, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                if(listener != null){
                    listener.OnSuccess(resultResponse);
                }else{
                    //Toast.makeText(context, resultResponse, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(listener != null){
                    listener.OnFail(error);
                }else{
                    //Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                if(bodys != null){
                    p = bodys;
                }
                return p;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> h= new HashMap<>();
                if(h != null){
                    h = headers;
                }else{
                    h = super.getHeaders();
                }
                return h;
            }
        };

        jsonRequest.setTag(TAG);
        VolleyRequestQueue.getInstance(context).addToRequestQueue(jsonRequest);
    }

    /**
     *헤더, body(json string), 파일(image) 정보를 입력할 경우 사용하는 함수
     * @param context
     * @param requestType
     * @param url
     * @param headers
     * @param body
     * @param multiParams
     * @param partType
     * @param listener
     */
    public static Object send(final Context context,
                              int requestType,
                              String url,
                              final Map<String, String> headers,
                              final Map<String, String> body,
                              final Map<String, DataPart> multiParams,
                              String partType,
                              final ResponseListener listener){
        Log.d("AuthApi", "request string >> " + url);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                if(listener != null){
                    listener.OnSuccess(resultResponse);
                }else{
                    //Toast.makeText(context, resultResponse, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(listener != null){
                    listener.OnFail(error);
                }else{
                    //Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                if(body != null){
                    p = body;
                }
                return p;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                //params.put("avatar", new DataPart("file_avatar.jpg",
                //        AppHelper.getFileDataFromDrawable(getBaseContext(), mAvatarImage.getDrawable()), "image/jpeg"));
                if(multiParams != null){
                    params = multiParams;
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> h= new HashMap<>();
                if(h != null){
                    h = headers;
                }else{
                    h = super.getHeaders();
                }
                return h;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                1000*60*2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));
        multipartRequest.setTag(""+ System.currentTimeMillis());
        if(partType != null && partType.length() > 0) {
            multipartRequest.setPartType(partType);
        }
        VolleyRequestQueue.getInstance(context).addToRequestQueue(multipartRequest);
        return multipartRequest.getTag();
    }

    public static int httpNetworkError(Context context, VolleyError error, NetworkResponse response){
        int status = response.statusCode;
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject Response = new JSONObject(result);
                String Status = Response.getString("status");
                String message = Response.getString("message");

                Log.e("Error Status", Status);
                Log.e("Error Message", message);

                if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                } else if (networkResponse.statusCode == 401) {
                    errorMessage = message+" Please login again";
                } else if (networkResponse.statusCode == 400) {
                    errorMessage = message+ " Check your inputs";
                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message+" Something is getting wrong";
                } else if (networkResponse.statusCode == 403) {
                    errorMessage = message+ " StatusCode 403";
                } else if (networkResponse.statusCode == 404) {
                    errorMessage = message+ " StatusCode 404";
                } else if (networkResponse.statusCode == 405) {
                    errorMessage = message+ " StatusCode 405";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Error", errorMessage);
        return status;
    }

    public static String getLocalRegion(Context context){
        String re = "";

        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        re = tm.getSimCountryIso();

        return re;
    }

    public static String getAppVersion(Context context) {
        String versionName = "1.0.0";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        }
        catch (Exception e) { e.printStackTrace();
        }

        return versionName;
    }

    public static String getAppMarket(Context context) {
        //1 : T 스토어, 2 : play 스토어, 3: apple 앱 스토어, 4: 지령대
        String ret = "2";

        if(context == null || context.getPackageManager() == null || TextUtils.isEmpty(context.getPackageName())) {
            return ret;
        }
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        if(TextUtils.isEmpty(installer)) {
            return ret;
        }
        Log.e("TN-APP-MARKET", "installer : " + installer);
        if(installer.toLowerCase().contains("android")) {
//            if("com.android.vending".equalsIgnoreCase(installer)) {
            ret = "2";
        }
        return ret;
    }
}
