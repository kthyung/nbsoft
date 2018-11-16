package com.nbsoft.tv.volley;

import java.io.IOException;

import okhttp3.Call;

public interface ResponseListenerokhttp {
    public void onResponse(Call call, okhttp3.Response response);
    public void onFailure(Call call, IOException e);
}
