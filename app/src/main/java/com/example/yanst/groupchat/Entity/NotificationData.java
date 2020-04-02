package com.example.yanst.groupchat.Entity;

public class NotificationData {
    private String user,
                   body,
                   title,
                   type;

    public NotificationData(String u, String b, String ti, String t) {
        user = u;
        body = b;
        title = ti;
        type = t;
    }

    public String getUser() { return user; }

    public void setUser(String u) { user = u; }

    public String getBody() { return body; }

    public void setBody(String b) { body = b; }

    public String getTitle() { return title; }

    public void setTitle(String t) { title = t; }

    public String getType() { return type; }

    public void setType(String t) { type = t; }
}
