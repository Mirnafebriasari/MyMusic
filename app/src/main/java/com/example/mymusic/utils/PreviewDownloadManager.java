package com.example.mymusic.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mymusic.database.AppDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Menangani download file preview MP3 ke penyimpanan internal aplikasi.
 *
 * Cara kerja:
 *  1. Buat direktori  getFilesDir()/previews/  (jika belum ada)
 *  2. Download file dengan OkHttp ke path  previews/{songId}.mp3
 *  3. Update kolom localPath di Room database
 *  4. Callback ke UI thread via Handler
 *
 * File disimpan di internal storage (tidak butuh izin WRITE_EXTERNAL_STORAGE).
 */
public class PreviewDownloadManager {

    private static final String TAG        = "PreviewDownload";
    private static final String DIR_NAME   = "previews";

    public interface DownloadCallback {
        /** Dipanggil di main thread saat download berhasil */
        void onSuccess(long songId, String localPath);
        /** Dipanggil di main thread saat download gagal */
        void onFailure(long songId, String errorMessage);
    }

    private final Context         context;
    private final AppDatabase     db;
    private final OkHttpClient    httpClient;
    private final ExecutorService executor;
    private final Handler         mainHandler;

    public PreviewDownloadManager(Context context) {
        this.context     = context.getApplicationContext();
        this.db          = AppDatabase.getInstance(context);
        this.httpClient  = new OkHttpClient();
        this.executor    = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Mulai download preview MP3 untuk lagu tertentu.
     *
     * @param songId   ID lagu (dipakai sebagai nama file)
     * @param previewUrl URL preview 30-detik dari Deezer
     * @param callback callback hasil download (opsional, boleh null)
     */
    public void download(long songId, String previewUrl, DownloadCallback callback) {
        if (previewUrl == null || previewUrl.isEmpty()) {
            if (callback != null) {
                mainHandler.post(() ->
                        callback.onFailure(songId, "Preview URL kosong"));
            }
            return;
        }

        executor.execute(() -> {
            File targetFile = getTargetFile(songId);

            // Kalau sudah ada, tidak perlu download ulang
            if (targetFile.exists() && targetFile.length() > 0) {
                String path = targetFile.getAbsolutePath();
                db.songDao().updateLocalPath(songId, path);
                if (callback != null) {
                    mainHandler.post(() -> callback.onSuccess(songId, path));
                }
                return;
            }

            try {
                Request request = new Request.Builder()
                        .url(previewUrl)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        throw new IOException("Response gagal: " + response.code());
                    }

                    // Pastikan direktori ada
                    File dir = targetFile.getParentFile();
                    if (dir != null && !dir.exists()) {
                        dir.mkdirs();
                    }

                    // Tulis ke file sementara dulu, rename setelah selesai
                    File tmpFile = new File(dir, songId + ".tmp");

                    try (InputStream  in  = response.body().byteStream();
                         FileOutputStream out = new FileOutputStream(tmpFile)) {

                        byte[] buf = new byte[8192];
                        int    len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        out.flush();
                    }

                    // Rename tmp → final
                    if (!tmpFile.renameTo(targetFile)) {
                        tmpFile.delete();
                        throw new IOException("Gagal rename file sementara");
                    }

                    String path = targetFile.getAbsolutePath();

                    // Simpan ke database
                    db.songDao().updateLocalPath(songId, path);

                    Log.d(TAG, "Download selesai: " + path);

                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(songId, path));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Download gagal untuk songId=" + songId, e);
                // Hapus file korup jika ada
                if (targetFile.exists()) targetFile.delete();

                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure(songId, e.getMessage()));
                }
            }
        });
    }

    /**
     * Hapus file lokal MP3 saat lagu dihapus dari favorit.
     */
    public void deleteLocalFile(long songId) {
        executor.execute(() -> {
            File file = getTargetFile(songId);
            if (file.exists()) {
                boolean deleted = file.delete();
                Log.d(TAG, "Delete file " + file.getName() + ": " + deleted);
            }
            // Bersihkan path di database
            db.songDao().updateLocalPath(songId, null);
        });
    }

    /**
     * Apakah file lokal untuk lagu ini sudah ada?
     */
    public boolean isDownloaded(long songId) {
        File file = getTargetFile(songId);
        return file.exists() && file.length() > 0;
    }

    // ------------------------------------------------------------------ //

    private File getTargetFile(long songId) {
        File dir = new File(context.getFilesDir(), DIR_NAME);
        return new File(dir, songId + ".mp3");
    }
}