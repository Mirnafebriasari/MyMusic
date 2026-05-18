package com.example.mymusic.model;

import com.google.gson.annotations.SerializedName;

public class Song {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("preview")
    private String preview;

    @SerializedName("artist")
    private Artist artist;

    @SerializedName("album")
    private Album album;

    public long getId()        { return id; }
    public String getTitle()   { return title; }
    public String getPreview() { return preview; }
    public Artist getArtist()  { return artist; }
    public Album getAlbum()    { return album; }
}