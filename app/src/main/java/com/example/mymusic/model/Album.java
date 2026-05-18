package com.example.mymusic.model;

import com.google.gson.annotations.SerializedName;

public class Album {

    @SerializedName("title")
    private String title;

    @SerializedName("cover_medium")
    private String coverMedium;

    public String getTitle() { return title; }
    public String getCoverMedium() { return coverMedium; }
}