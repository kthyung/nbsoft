package com.nbsoft.tvofall.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

public class YoutuberBookmark implements Serializable {
    @SerializedName("bookmark")
    HashMap<String, FirebaseDataItem> dataMap;

    public YoutuberBookmark(){

    }

    public HashMap<String, FirebaseDataItem> getDataMap() {
        return dataMap;
    }

    public void setDataMap(HashMap<String, FirebaseDataItem> dataMap) {
        this.dataMap = dataMap;
    }
}
