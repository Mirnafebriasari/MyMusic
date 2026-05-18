package com.example.mymusic.api;

import com.example.mymusic.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiClient {

    public static OkHttpClient getClient() {

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {

                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("X-RapidAPI-Key", Constants.API_KEY)
                            .addHeader("X-RapidAPI-Host", Constants.API_HOST)
                            .build();

                    return chain.proceed(request);
                })
                .build();
    }
}