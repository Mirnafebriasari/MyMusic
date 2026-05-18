package com.example.mymusic.api;

import com.example.mymusic.model.DeezerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("search")
    Call<DeezerResponse> searchSongs(
            @Query("q") String query
    );
}