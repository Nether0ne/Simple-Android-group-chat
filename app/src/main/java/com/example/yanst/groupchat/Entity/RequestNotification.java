package com.example.yanst.groupchat.Entity;

import com.google.gson.annotations.SerializedName;

public class RequestNotification {
    @SerializedName("to") //  "to" changed to token
    private String to;

    @SerializedName("data")
    private NotificationData data;

    public NotificationData NotificationData() {
        return data;
    }

    public void setNotificationData(NotificationData nd) {
        data = nd;
    }

    public String getToken() {
        return to;
    }

    public void setToken(String t) {
        to = t;
    }
}
