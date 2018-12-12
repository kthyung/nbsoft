package com.nbsoft.tvofall.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

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
