package com.nbsoft.sample.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DataItemDetail implements Serializable {
    @SerializedName("name")                         String name;
    @SerializedName("content")                     String content;
    @SerializedName("imgUrl")                     String imgUrl;
    @SerializedName("webUrl")                     String webUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
