package com.nbsoft.sample.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FirebaseNoticeItem implements Serializable {
    @SerializedName("date")                         String date;
    @SerializedName("content")                      String content;

    public FirebaseNoticeItem(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
