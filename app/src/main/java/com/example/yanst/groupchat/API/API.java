package com.example.yanst.groupchat.API;

import com.example.yanst.groupchat.BuildConfig;
import com.example.yanst.groupchat.Entity.RequestNotification;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface API {
    @Headers({"Authorization: key=" + BuildConfig.ApiKey,
            "Content-Type: application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendChatNotification(@Body RequestNotification requestNotification);
}
