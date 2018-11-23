package com.nbsoft.tv.model;

import android.support.v4.util.LongSparseArray;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class YoutuberHistory implements Serializable {
    @SerializedName("history")
    List<YoutuberHistoryItem> dataMap;

    public YoutuberHistory(){

    }

    public List<YoutuberHistoryItem> getDataMap() {
        return dataMap;
    }

    public void setDataMap(List<YoutuberHistoryItem> dataMap) {
        this.dataMap = dataMap;
    }
}
