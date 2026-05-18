package com.example.mymusic.repository;

import androidx.lifecycle.LiveData;

import com.example.mymusic.database.AppDatabase;
import com.example.mymusic.database.SongDao;
import com.example.mymusic.database.SongEntity;
import com.example.mymusic.utils.PreviewDownloadManager;

import java.util.List;

public class SongRepository {

    private final SongDao                songDao;
    private final PreviewDownloadManager downloadManager;

    public SongRepository(AppDatabase database, android.content.Context context) {
        this.songDao         = database.songDao();
        this.downloadManager = new PreviewDownloadManager(context);
    }

    /**
     * Insert lagu ke favorit dan otomatis download preview MP3-nya.
     */
    public void insertSong(SongEntity song,
                           PreviewDownloadManager.DownloadCallback callback) {
        songDao.insertSong(song);
        downloadManager.download(song.getId(), song.getPreview(), callback);
    }

    /** Overload tanpa callback */
    public void insertSong(SongEntity song) {
        insertSong(song, null);
    }

    /**
     * Hapus lagu dari favorit dan hapus file MP3 lokal-nya.
     */
    public void deleteSong(SongEntity song) {
        songDao.deleteSong(song);
        downloadManager.deleteLocalFile(song.getId());
    }

    public LiveData<List<SongEntity>> getAllSongs() {
        return songDao.getAllSongs();
    }

    public List<SongEntity> getAllSongsSync() {
        return songDao.getAllSongsSync();
    }

    public boolean isSongFavorite(long songId) {
        return songDao.isSongExists(songId) > 0;
    }

    public boolean isDownloaded(long songId) {
        return downloadManager.isDownloaded(songId);
    }
}