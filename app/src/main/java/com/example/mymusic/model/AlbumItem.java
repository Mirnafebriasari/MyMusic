package com.example.mymusic.model;

public class AlbumItem {

    private final String title;
    private final String coverUrl;
    private final String artistName;
    private final int    songCount;

    public AlbumItem(String title, String coverUrl, String artistName, int songCount) {
        this.title      = title;
        this.coverUrl   = coverUrl;
        this.artistName = artistName;
        this.songCount  = songCount;
    }

    public String getTitle()      { return title; }
    public String getCoverUrl()   { return coverUrl; }
    public String getArtistName() { return artistName; }
    public int    getSongCount()  { return songCount; }
}