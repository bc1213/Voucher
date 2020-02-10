package com.g12.apple.appleTvLogin.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TokenTransferRequest implements Serializable {

    // The following information would be passed from android app
    @SerializedName("secretToken")
    @Expose
    private String token;

    @SerializedName("deviceID")
    @Expose
    private String deviceID;

    @SerializedName("userid")
    @Expose
    private String userID;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String myPeerId) {
        this.deviceID = myPeerId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}