package com.nbsoft.sample.volley;

import com.android.volley.VolleyError;

public interface ResponseListener {
    public void OnSuccess(String jsonString);
    public void OnFail(VolleyError error);
}
