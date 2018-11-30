package com.nbsoft.tv.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class YoutuberRequest implements Serializable {
    @SerializedName("request")
    List<FirebaseRequestItem> dataMap;

    public YoutuberRequest(){

    }

    public List<FirebaseRequestItem> getDataMap() {
        return dataMap;
    }

    public void setDataMap(List<FirebaseRequestItem> dataMap) {
        this.dataMap = dataMap;
    }
}
