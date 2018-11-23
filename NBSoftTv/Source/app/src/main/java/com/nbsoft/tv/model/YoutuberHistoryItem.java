package com.nbsoft.tv.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class YoutuberHistoryItem implements Serializable {
    @SerializedName("pid")
    String pid;

    @SerializedName("pTitle")
    String pTitle;

    @SerializedName("pDescription")
    String pDescription;


    @SerializedName("thumbnail")
    String thumbnail;

    @SerializedName("name")
    String name;

    @SerializedName("vid")
    String vid;

    @SerializedName("history_date")
    Long historyDate;

    public YoutuberHistoryItem(){

    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpDescription() {
        return pDescription;
    }

    public void setpDescription(String pDescription) {
        this.pDescription = pDescription;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public Long getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Long historyDate) {
        this.historyDate = historyDate;
    }
}
