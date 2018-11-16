package com.nbsoft.tv.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DataItemList implements Serializable {
    @SerializedName("id")                         int id;
    @SerializedName("name")
    String name;
    @SerializedName("content")
    String content;
    @SerializedName("imgUrl")
    String imgUrl;

    public DataItemList(){

    }

    public DataItemList(int id, String name, String content, String imgUrl){
        this.id = id;
        this.name = name;
        this.content = content;
        this.imgUrl = imgUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
}
