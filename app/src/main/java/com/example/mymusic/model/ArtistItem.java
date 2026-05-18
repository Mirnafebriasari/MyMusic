package com.example.mymusic.model;

public class ArtistItem {

    private final String name;
    private final String pictureUrl;
    private final int    songCount;

    public ArtistItem(String name, String pictureUrl, int songCount) {
        this.name       = name;
        this.pictureUrl = pictureUrl;
        this.songCount  = songCount;
    }

    public String getName()       { return name; }
    public String getPictureUrl() { return pictureUrl; }
    public int    getSongCount()  { return songCount; }
}