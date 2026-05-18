package com.example.mymusic.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(SongEntity song);

    @Delete
    void deleteSong(SongEntity song);

    @Query("SELECT * FROM favorite_songs")
    LiveData<List<SongEntity>> getAllSongs();

    @Query("SELECT * FROM favorite_songs")
    List<SongEntity> getAllSongsSync();

    @Query("SELECT COUNT(*) FROM favorite_songs WHERE id = :songId")
    int isSongExists(long songId);

    @Query("SELECT * FROM favorite_songs WHERE id = :songId LIMIT 1")
    SongEntity getSongById(long songId);

    @Query("UPDATE favorite_songs SET localPath = :localPath WHERE id = :songId")
    void updateLocalPath(long songId, String localPath);
}