package com.example.mymusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeezerResponse {

    @SerializedName("data")
    private List<Song> data;

    public List<Song> getData() { return data; }
}