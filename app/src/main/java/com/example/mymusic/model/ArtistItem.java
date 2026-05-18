package com.example.mymusic.model;

/**
 * Model untuk menampilkan satu baris di tab Artis.
 * Berisi nama artis, foto artis, dan jumlah lagu milik artis tersebut.
 */
public class ArtistItem {

    private final String name;
    private final String pictureUrl; // bisa dari Artist.getPictureMedium() atau album cover
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