package com.example.mymusic.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_songs")
public class SongEntity {

    @PrimaryKey
    private long id;

    private String title;
    private String artist;
    private String cover;
    private String preview;

    private String localPath;

    public SongEntity(long id,
                      String title,
                      String artist,
                      String cover,
                      String preview) {
        this.id       = id;
        this.title    = title;
        this.artist   = artist;
        this.cover    = cover;
        this.preview  = preview;
        this.localPath = null;
    }

    // ---- Getters ----
    public long   getId()        { return id; }
    public String getTitle()     { return title; }
    public String getArtist()    { return artist; }
    public String getCover()     { return cover; }
    public String getPreview()   { return preview; }
    public String getLocalPath() { return localPath; }

    public void setLocalPath(String localPath) { this.localPath = localPath; }


    public String getPlaybackPath() {
        if (localPath != null && !localPath.isEmpty()) {
            return "file://" + localPath;
        }
        return preview;
    }

    public boolean isDownloaded() {
        return localPath != null && !localPath.isEmpty();
    }
}