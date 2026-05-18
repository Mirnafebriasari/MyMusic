package com.example.mymusic.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entitas Room untuk lagu favorit.
 *
 * PERUBAHAN dari versi sebelumnya:
 *  • Tambah kolom `localPath` — menyimpan path file MP3 yang sudah didownload.
 *    Jika null/kosong berarti belum didownload; putar langsung dari URL preview.
 *
 * MIGRASI DATABASE:
 *  Versi database dinaikkan dari 1 → 2 (lihat AppDatabase.java).
 *  Kolom baru bersifat nullable sehingga data lama tetap valid.
 */
@Entity(tableName = "favorite_songs")
public class SongEntity {

    @PrimaryKey
    private long id;

    private String title;
    private String artist;
    private String cover;
    private String preview;   // URL online

    /** Path file MP3 lokal. Null = belum didownload. */
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

    // ---- Setters (Room butuh setter atau public field) ----
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    /**
     * Kembalikan path yang sebaiknya digunakan untuk memutar audio:
     *  - Jika ada file lokal → gunakan path lokal (offline-ready)
     *  - Jika tidak → fallback ke URL preview online
     */
    public String getPlaybackPath() {
        if (localPath != null && !localPath.isEmpty()) {
            return "file://" + localPath;
        }
        return preview;
    }

    /** Apakah MP3 preview sudah tersimpan secara lokal? */
    public boolean isDownloaded() {
        return localPath != null && !localPath.isEmpty();
    }
}