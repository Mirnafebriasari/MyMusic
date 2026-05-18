package com.example.mymusic.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.IOException;

public class MusicPlayerManager {

    private MediaPlayer mediaPlayer;
    private boolean     finished = false;

    public void playMusic(Context context, String previewUrl) {
        try {
            stopMusic();
            finished    = false;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(previewUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> finished = true);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal memutar lagu", Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finished = false;
    }

    public void seekTo(int progressPercent) {
        if (mediaPlayer != null) {
            int target = (int) ((progressPercent / 100f) * mediaPlayer.getDuration());
            mediaPlayer.seekTo(target);
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isFinished() {
        return finished;
    }
}