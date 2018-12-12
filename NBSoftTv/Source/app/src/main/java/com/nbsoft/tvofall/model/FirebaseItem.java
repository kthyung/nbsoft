package com.nbsoft.tvofall.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class FirebaseItem implements Serializable {
    @SerializedName("notice")
    List<FirebaseNoticeItem> notice;
    @SerializedName("data")
    List<FirebaseDataItem> data;

    public FirebaseItem(){

    }

    public List<FirebaseNoticeItem> getNotice() {
        return notice;
    }

    public void setNotice(List<FirebaseNoticeItem> notice) {
        this.notice = notice;
    }

    public List<FirebaseDataItem> getData() {
        return data;
    }

    public void setData(List<FirebaseDataItem> data) {
        this.data = data;
    }
}
