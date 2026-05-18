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

public class PreviewDownloadManager {

    private static final String TAG        = "PreviewDownload";
    private static final String DIR_NAME   = "previews";

    public interface DownloadCallback {
        void onSuccess(long songId, String localPath);
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

                    File dir = targetFile.getParentFile();
                    if (dir != null && !dir.exists()) {
                        dir.mkdirs();
                    }

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

                    if (!tmpFile.renameTo(targetFile)) {
                        tmpFile.delete();
                        throw new IOException("Gagal rename file sementara");
                    }

                    String path = targetFile.getAbsolutePath();

                    db.songDao().updateLocalPath(songId, path);

                    Log.d(TAG, "Download selesai: " + path);

                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(songId, path));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Download gagal untuk songId=" + songId, e);
                if (targetFile.exists()) targetFile.delete();

                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure(songId, e.getMessage()));
                }
            }
        });
    }

    public void deleteLocalFile(long songId) {
        executor.execute(() -> {
            File file = getTargetFile(songId);
            if (file.exists()) {
                boolean deleted = file.delete();
                Log.d(TAG, "Delete file " + file.getName() + ": " + deleted);
            }
            db.songDao().updateLocalPath(songId, null);
        });
    }

    public boolean isDownloaded(long songId) {
        File file = getTargetFile(songId);
        return file.exists() && file.length() > 0;
    }


    private File getTargetFile(long songId) {
        File dir = new File(context.getFilesDir(), DIR_NAME);
        return new File(dir, songId + ".mp3");
    }
}