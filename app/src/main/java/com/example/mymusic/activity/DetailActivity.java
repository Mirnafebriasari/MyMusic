package com.example.mymusic.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.database.AppDatabase;
import com.example.mymusic.database.SongEntity;
import com.example.mymusic.databinding.ActivityDetailBinding;
import com.example.mymusic.player.MusicPlayerManager;
import com.example.mymusic.repository.SongRepository;
import com.example.mymusic.utils.Constants;
import com.example.mymusic.utils.PreviewDownloadManager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private MusicPlayerManager    musicPlayerManager;
    private SongRepository        repository;
    private final Handler         seekHandler = new Handler(Looper.getMainLooper());

    private boolean isFavorite   = false;
    private boolean isRepeat     = false;
    private int     currentIndex = 0;

    private ArrayList<Long>   ids;
    private ArrayList<String> titles;
    private ArrayList<String> artists;
    private ArrayList<String> covers;
    private ArrayList<String> previews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        musicPlayerManager = new MusicPlayerManager();
        repository = new SongRepository(AppDatabase.getInstance(this), this);

        currentIndex = getIntent().getIntExtra(Constants.EXTRA_POSITION, 0);
        ids      = (ArrayList<Long>)   getIntent().getSerializableExtra(Constants.EXTRA_IDS);
        titles   = (ArrayList<String>) getIntent().getSerializableExtra(Constants.EXTRA_TITLES);
        artists  = (ArrayList<String>) getIntent().getSerializableExtra(Constants.EXTRA_ARTISTS);
        covers   = (ArrayList<String>) getIntent().getSerializableExtra(Constants.EXTRA_COVERS);
        previews = (ArrayList<String>) getIntent().getSerializableExtra(Constants.EXTRA_PREVIEWS);

        if (ids == null) {
            ids      = new ArrayList<>();
            titles   = new ArrayList<>();
            artists  = new ArrayList<>();
            covers   = new ArrayList<>();
            previews = new ArrayList<>();

            ids.add(getIntent().getLongExtra(Constants.EXTRA_ID, 0));
            titles.add(getIntent().getStringExtra(Constants.EXTRA_TITLE));
            artists.add(getIntent().getStringExtra(Constants.EXTRA_ARTIST));
            covers.add(getIntent().getStringExtra(Constants.EXTRA_COVER));
            previews.add(getIntent().getStringExtra(Constants.EXTRA_PREVIEW));
            currentIndex = 0;
        }

        loadSong(currentIndex);
        setupButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        seekHandler.removeCallbacks(seekRunnable);
        musicPlayerManager.stopMusic();
    }

    // ------------------------------------------------------------------ //

    private void loadSong(int index) {
        seekHandler.removeCallbacks(seekRunnable);
        musicPlayerManager.stopMusic();

        binding.btnPlay.setVisibility(View.VISIBLE);
        binding.btnPause.setVisibility(View.GONE);
        binding.seekBar.setProgress(0);
        binding.tvCurrentTime.setText(getString(R.string.time_zero));
        binding.tvDuration.setText(getString(R.string.time_zero));

        binding.tvTitle.setText(titles.get(index));
        binding.tvArtist.setText(artists.get(index));

        Glide.with(this)
                .load(covers.get(index))
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .centerCrop()
                .into(binding.imgCover);

        updateFavoriteIcon();

        String path = resolvePlaybackPath(index);
        if (path != null && !path.isEmpty()) {
            musicPlayerManager.playMusic(this, path);
            binding.btnPlay.setVisibility(View.GONE);
            binding.btnPause.setVisibility(View.VISIBLE);
            startSeekBarUpdate();
        }
    }

    private String resolvePlaybackPath(int index) {
        long songId = ids.get(index);
        if (repository.isDownloaded(songId)) {
            return "file://" + getFilesDir().getAbsolutePath()
                    + "/previews/" + songId + ".mp3";
        }
        return previews.get(index);
    }

    // ------------------------------------------------------------------ //

    private void setupButtons() {

        binding.btnBack.setOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        binding.btnPlay.setOnClickListener(v -> {
            String path = resolvePlaybackPath(currentIndex);
            if (path != null && !path.isEmpty()) {
                musicPlayerManager.playMusic(this, path);
                binding.btnPlay.setVisibility(View.GONE);
                binding.btnPause.setVisibility(View.VISIBLE);
                startSeekBarUpdate();
            } else {
                Toast.makeText(this,
                        getString(R.string.msg_no_preview),
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnPause.setOnClickListener(v -> {
            musicPlayerManager.pauseMusic();
            binding.btnPlay.setVisibility(View.VISIBLE);
            binding.btnPause.setVisibility(View.GONE);
        });

        binding.btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                loadSong(currentIndex);
            } else {
                musicPlayerManager.seekTo(0);
                if (!musicPlayerManager.isPlaying()) {
                    musicPlayerManager.playMusic(this, resolvePlaybackPath(currentIndex));
                    binding.btnPlay.setVisibility(View.GONE);
                    binding.btnPause.setVisibility(View.VISIBLE);
                    startSeekBarUpdate();
                }
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentIndex < ids.size() - 1) {
                currentIndex++;
                loadSong(currentIndex);
            } else {
                Toast.makeText(this, "Ini lagu terakhir", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            int tint = isRepeat ? 0xFFBB86FC : 0xFF888888;
            binding.btnRepeat.setColorFilter(tint);
            Toast.makeText(this,
                    isRepeat ? "Repeat ON" : "Repeat OFF",
                    Toast.LENGTH_SHORT).show();
        });

        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) musicPlayerManager.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s)  {}
        });
    }

    // ------------------------------------------------------------------ //

    private void startSeekBarUpdate() {
        seekHandler.post(seekRunnable);
    }

    private final Runnable seekRunnable = new Runnable() {
        @Override
        public void run() {
            if (musicPlayerManager.isPlaying()) {
                int current  = musicPlayerManager.getCurrentPosition();
                int duration = musicPlayerManager.getDuration();
                if (duration > 0) {
                    binding.seekBar.setProgress(
                            (int) ((current / (float) duration) * 100));
                    binding.tvCurrentTime.setText(formatTime(current));
                    binding.tvDuration.setText(formatTime(duration));
                }
                seekHandler.postDelayed(this, 500);
            } else if (musicPlayerManager.isFinished()) {
                if (isRepeat) {
                    loadSong(currentIndex);
                } else if (currentIndex < ids.size() - 1) {
                    currentIndex++;
                    loadSong(currentIndex);
                } else {
                    binding.btnPlay.setVisibility(View.VISIBLE);
                    binding.btnPause.setVisibility(View.GONE);
                    binding.seekBar.setProgress(0);
                    binding.tvCurrentTime.setText(getString(R.string.time_zero));
                }
            }
        }
    };

    private String formatTime(int ms) {
        return String.format(Locale.getDefault(), "%d:%02d",
                (ms / 1000) / 60, (ms / 1000) % 60);
    }

    // ------------------------------------------------------------------ //

    private void toggleFavorite() {
        long   songId  = ids.get(currentIndex);
        String title   = titles.get(currentIndex);
        String artist  = artists.get(currentIndex);
        String cover   = covers.get(currentIndex);
        String preview = previews.get(currentIndex);

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean fav = repository.isSongFavorite(songId);
            SongEntity entity = new SongEntity(songId, title, artist, cover, preview);

            if (fav) {
                repository.deleteSong(entity);
                runOnUiThread(() -> {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(this,
                            getString(R.string.msg_removed_favorite),
                            Toast.LENGTH_SHORT).show();
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                getString(R.string.msg_added_favorite),
                                Toast.LENGTH_SHORT).show()
                );

                repository.insertSong(entity, null);

                runOnUiThread(() -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                });
            }
        });
    }

    private void updateFavoriteIcon() {
        long songId = ids.get(currentIndex);
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean fav = repository.isSongFavorite(songId);
            runOnUiThread(() -> {
                isFavorite = fav;
                int tint = fav ? 0xFFBB86FC : 0xFF888888;
                binding.btnFavorite.setColorFilter(tint);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}