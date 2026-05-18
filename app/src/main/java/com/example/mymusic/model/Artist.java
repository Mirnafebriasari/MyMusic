package com.example.mymusic.model;

import com.google.gson.annotations.SerializedName;

public class Artist {

    @SerializedName("name")
    private String name;

    @SerializedName("picture_medium")
    private String pictureMedium;

    public String getName() { return name; }
    public String getPictureMedium() { return pictureMedium; }
}