package com.nbsoft.tvofall.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FirebaseRequestItem implements Serializable {
    @SerializedName("date")
    String date;
    @SerializedName("cname")
    String cname;
    @SerializedName("account")
    String account;

    public FirebaseRequestItem(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCName() {
        return cname;
    }

    public void setCName(String cname) {
        this.cname = cname;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
