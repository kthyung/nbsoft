package com.nbsoft.tv.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FirebaseNoticeItem implements Serializable {
    @SerializedName("date")
    String date;
    @SerializedName("title")
    String title;
    @SerializedName("content")
    String content;

    boolean isExpand = false;

    public FirebaseNoticeItem(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }
}
