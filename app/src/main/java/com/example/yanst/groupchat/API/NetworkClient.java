package com.example.yanst.groupchat.API;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    public static final String BASE_URL = "https://fcm.googleapis.com/";
    public static Retrofit retrofit;

    // Возвращение Retrofit клиента
    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Конвертор JSON
                    .build();
        }
        return retrofit;
    }

}
