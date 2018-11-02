package com.nbsoft.sample.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FirebaseDataItem implements Serializable {
    @SerializedName("type")                         String type;
    @SerializedName("name")                         String name;
    @SerializedName("cid")                          String cid;
    @SerializedName("thumbnail")                    String thumbnail;

    public FirebaseDataItem(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
