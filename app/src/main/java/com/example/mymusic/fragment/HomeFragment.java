package com.example.mymusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mymusic.R;
import com.example.mymusic.activity.DetailActivity;
import com.example.mymusic.adapter.AlbumAdapter;
import com.example.mymusic.adapter.ArtistAdapter;
import com.example.mymusic.adapter.SongAdapter;
import com.example.mymusic.api.RetrofitInstance;
import com.example.mymusic.database.AppDatabase;
import com.example.mymusic.database.SongEntity;
import com.example.mymusic.databinding.FragmentHomeBinding;
import com.example.mymusic.model.AlbumItem;
import com.example.mymusic.model.ArtistItem;
import com.example.mymusic.model.DeezerResponse;
import com.example.mymusic.model.Song;
import com.example.mymusic.repository.SongRepository;
import com.example.mymusic.utils.Constants;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final int SORT_DEFAULT = 0;
    private static final int SORT_TITLE   = 1;
    private static final int SORT_ARTIST  = 2;

    private int currentSort = SORT_DEFAULT;
    private int currentTab  = 0;
    private boolean isFilteredMode = false;

    private FragmentHomeBinding binding;
    private SongRepository      repository;

    private SongAdapter   songAdapter;
    private ArtistAdapter artistAdapter;
    private AlbumAdapter  albumAdapter;

    private final List<Song>       songList   = new ArrayList<>();
    private final List<ArtistItem> artistList = new ArrayList<>();
    private final List<AlbumItem>  albumList  = new ArrayList<>();
    private final List<Song>       masterList = new ArrayList<>();

    // ------------------------------------------------------------------ //
    //  Lifecycle
    // ------------------------------------------------------------------ //

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new SongRepository(
                AppDatabase.getInstance(requireContext()), requireContext());

        setupAdapters();
        setupTabs();
        setupSearch();
        setupSortMenu();
        setupPlayAll();

        fetchFromApi("top hits");

        // Satu listener retry dipakai oleh dua tombol:
        // - btnRetry    → di dalam layoutError (tidak ada data offline)
        // - btnHistory  → icon history di search bar (ada data offline)
        View.OnClickListener retryClick = v -> {
            binding.layoutError.setVisibility(View.GONE);
            binding.bannerOffline.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.layoutPlayAll.setVisibility(View.GONE);

            String query = binding.etSearch.getText().toString().trim();
            fetchFromApi(query.isEmpty() ? "top hits" : query);
        };

        binding.btnRetry.setOnClickListener(retryClick);
        // btnHistory sekarang sekaligus berfungsi sebagai tombol retry saat offline
        binding.btnHistory.setOnClickListener(retryClick);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ------------------------------------------------------------------ //
    //  Setup
    // ------------------------------------------------------------------ //

    private void setupAdapters() {
        songAdapter = new SongAdapter(songList, song -> {
            int position = songList.indexOf(song);

            ArrayList<Long>   ids      = new ArrayList<>();
            ArrayList<String> titles   = new ArrayList<>();
            ArrayList<String> artists  = new ArrayList<>();
            ArrayList<String> covers   = new ArrayList<>();
            ArrayList<String> previews = new ArrayList<>();

            for (Song s : songList) {
                ids.add(s.getId());
                titles.add(s.getTitle());
                artists.add(s.getArtist() != null ? s.getArtist().getName() : "Unknown Artist");
                covers.add(s.getAlbum()   != null ? s.getAlbum().getCoverMedium() : "");
                previews.add(s.getPreview() != null ? s.getPreview() : "");
            }

            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(Constants.EXTRA_ID,       song.getId());
            intent.putExtra(Constants.EXTRA_TITLE,    song.getTitle());
            intent.putExtra(Constants.EXTRA_ARTIST,
                    song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist");
            intent.putExtra(Constants.EXTRA_COVER,
                    song.getAlbum()   != null ? song.getAlbum().getCoverMedium() : "");
            intent.putExtra(Constants.EXTRA_PREVIEW,  song.getPreview());
            intent.putExtra(Constants.EXTRA_POSITION, position);
            intent.putExtra(Constants.EXTRA_IDS,      ids);
            intent.putExtra(Constants.EXTRA_TITLES,   titles);
            intent.putExtra(Constants.EXTRA_ARTISTS,  artists);
            intent.putExtra(Constants.EXTRA_COVERS,   covers);
            intent.putExtra(Constants.EXTRA_PREVIEWS, previews);
            startActivity(intent);
        });

        artistAdapter = new ArtistAdapter(artistList, artist -> {
            filterSongByArtist(artist.getName());
            binding.etSearch.setText(artist.getName());
            isFilteredMode = true;
            currentTab = 0;
            binding.recyclerView.setAdapter(songAdapter);
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
        });

        albumAdapter = new AlbumAdapter(albumList, album -> {
            filterSongByAlbum(album.getTitle());
            binding.etSearch.setText(album.getTitle());
            isFilteredMode = true;
            currentTab = 0;
            binding.recyclerView.setAdapter(songAdapter);
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(songAdapter);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_songs));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_artists));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_albums));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                if (isFilteredMode) {
                    isFilteredMode = false;
                    return;
                }
                switch (currentTab) {
                    case 0: showSongTab();   break;
                    case 1: showArtistTab(); break;
                    case 2: showAlbumTab();  break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrentTab(s.toString().trim());
            }
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.etSearch.getText().toString().trim();
            binding.layoutError.setVisibility(View.GONE);
            binding.bannerOffline.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.layoutPlayAll.setVisibility(View.GONE);
            fetchFromApi(query.isEmpty() ? "top hits" : query);
            return true;
        });
    }

    private void setupSortMenu() {
        binding.btnSortMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), binding.btnSortMenu);
            popup.getMenu().add(0, SORT_DEFAULT, 0, getString(R.string.sort_default));
            popup.getMenu().add(0, SORT_TITLE,   1, getString(R.string.sort_title));
            popup.getMenu().add(0, SORT_ARTIST,  2, getString(R.string.sort_artist));

            popup.setOnMenuItemClickListener(item -> {
                currentSort = item.getItemId();
                applySortToCurrentTab();
                return true;
            });
            popup.show();
        });
    }

    private void setupPlayAll() {
        binding.layoutPlayAll.setOnClickListener(v -> {
            if (currentTab != 0 || songList.isEmpty()) return;

            ArrayList<Long>   ids      = new ArrayList<>();
            ArrayList<String> titles   = new ArrayList<>();
            ArrayList<String> artists  = new ArrayList<>();
            ArrayList<String> covers   = new ArrayList<>();
            ArrayList<String> previews = new ArrayList<>();

            for (Song s : songList) {
                ids.add(s.getId());
                titles.add(s.getTitle());
                artists.add(s.getArtist() != null ? s.getArtist().getName() : "Unknown");
                covers.add(s.getAlbum()   != null ? s.getAlbum().getCoverMedium() : "");
                previews.add(s.getPreview() != null ? s.getPreview() : "");
            }

            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(Constants.EXTRA_POSITION, 0);
            intent.putExtra(Constants.EXTRA_ID,       ids.get(0));
            intent.putExtra(Constants.EXTRA_TITLE,    titles.get(0));
            intent.putExtra(Constants.EXTRA_ARTIST,   artists.get(0));
            intent.putExtra(Constants.EXTRA_COVER,    covers.get(0));
            intent.putExtra(Constants.EXTRA_PREVIEW,  previews.get(0));
            intent.putExtra(Constants.EXTRA_IDS,      ids);
            intent.putExtra(Constants.EXTRA_TITLES,   titles);
            intent.putExtra(Constants.EXTRA_ARTISTS,  artists);
            intent.putExtra(Constants.EXTRA_COVERS,   covers);
            intent.putExtra(Constants.EXTRA_PREVIEWS, previews);
            startActivity(intent);
        });
    }

    // ------------------------------------------------------------------ //
    //  Tab display
    // ------------------------------------------------------------------ //

    private void showSongTab() {
        binding.recyclerView.setAdapter(songAdapter);
        songList.clear();
        songList.addAll(masterList);
        songAdapter.notifyDataSetChanged();

        binding.layoutPlayAll.setVisibility(View.VISIBLE);
        binding.btnPlayAllIcon.setVisibility(View.VISIBLE);
        binding.btnPlayAllLabel.setVisibility(View.VISIBLE);
        updateSongCount();
    }

    private void showArtistTab() {
        binding.recyclerView.setAdapter(artistAdapter);
        List<ArtistItem> items = buildArtistItems(masterList);
        artistAdapter.updateList(items);

        binding.layoutPlayAll.setVisibility(View.VISIBLE);
        binding.btnPlayAllIcon.setVisibility(View.GONE);
        binding.btnPlayAllLabel.setVisibility(View.GONE);
        if (binding != null)
            binding.tvSongCount.setText(items.size() + " artists");
    }

    private void showAlbumTab() {
        binding.recyclerView.setAdapter(albumAdapter);
        List<AlbumItem> items = buildAlbumItems(masterList);
        albumAdapter.updateList(items);

        binding.layoutPlayAll.setVisibility(View.VISIBLE);
        binding.btnPlayAllIcon.setVisibility(View.GONE);
        binding.btnPlayAllLabel.setVisibility(View.GONE);
        if (binding != null)
            binding.tvSongCount.setText(items.size() + " albums");
    }

    // ------------------------------------------------------------------ //
    //  Builders
    // ------------------------------------------------------------------ //

    private List<ArtistItem> buildArtistItems(List<Song> songs) {
        Map<String, List<Song>> byArtist = new LinkedHashMap<>();
        for (Song s : songs) {
            String name = s.getArtist() != null ? s.getArtist().getName() : "Unknown Artist";
            byArtist.computeIfAbsent(name, k -> new ArrayList<>()).add(s);
        }
        List<ArtistItem> result = new ArrayList<>();
        for (Map.Entry<String, List<Song>> entry : byArtist.entrySet()) {
            Song first = entry.getValue().get(0);
            String pic = (first.getArtist() != null
                    && first.getArtist().getPictureMedium() != null
                    && !first.getArtist().getPictureMedium().isEmpty())
                    ? first.getArtist().getPictureMedium()
                    : (first.getAlbum() != null ? first.getAlbum().getCoverMedium() : "");
            result.add(new ArtistItem(entry.getKey(), pic, entry.getValue().size()));
        }
        return result;
    }

    private List<AlbumItem> buildAlbumItems(List<Song> songs) {
        Map<String, List<Song>> byAlbum = new LinkedHashMap<>();
        for (Song s : songs) {
            String albumTitle = (s.getAlbum() != null
                    && s.getAlbum().getTitle() != null
                    && !s.getAlbum().getTitle().isEmpty())
                    ? s.getAlbum().getTitle() : "Unknown Album";
            byAlbum.computeIfAbsent(albumTitle, k -> new ArrayList<>()).add(s);
        }
        List<AlbumItem> result = new ArrayList<>();
        for (Map.Entry<String, List<Song>> entry : byAlbum.entrySet()) {
            Song first    = entry.getValue().get(0);
            String cover  = first.getAlbum()  != null ? first.getAlbum().getCoverMedium() : "";
            String artist = first.getArtist() != null ? first.getArtist().getName()        : "Unknown";
            result.add(new AlbumItem(entry.getKey(), cover, artist, entry.getValue().size()));
        }
        return result;
    }

    // ------------------------------------------------------------------ //
    //  Sort
    // ------------------------------------------------------------------ //

    private void applySortToCurrentTab() {
        switch (currentTab) {
            case 1: applySortArtist(); break;
            case 2: applySortAlbum();  break;
            default: applySortSong();  break;
        }
    }

    private void applySortSong() {
        switch (currentSort) {
            case SORT_TITLE:
                songList.sort(Comparator.comparing(s -> s.getTitle().toLowerCase()));
                break;
            case SORT_ARTIST:
                songList.sort(Comparator.comparing(
                        s -> (s.getArtist() != null ? s.getArtist().getName().toLowerCase() : "")));
                break;
            default:
                showSongTab(); return;
        }
        songAdapter.notifyDataSetChanged();
    }

    private void applySortArtist() {
        List<ArtistItem> items = buildArtistItems(masterList);
        if (currentSort == SORT_TITLE || currentSort == SORT_ARTIST)
            items.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
        artistAdapter.updateList(items);
        if (binding != null) binding.tvSongCount.setText(items.size() + " artists");
    }

    private void applySortAlbum() {
        List<AlbumItem> items = buildAlbumItems(masterList);
        if (currentSort == SORT_TITLE)
            items.sort(Comparator.comparing(a -> a.getTitle().toLowerCase()));
        else if (currentSort == SORT_ARTIST)
            items.sort(Comparator.comparing(a -> a.getArtistName().toLowerCase()));
        albumAdapter.updateList(items);
        if (binding != null) binding.tvSongCount.setText(items.size() + " albums");
    }

    // ------------------------------------------------------------------ //
    //  Filter
    // ------------------------------------------------------------------ //

    private void filterCurrentTab(String query) {
        switch (currentTab) {
            case 1: filterArtistTab(query); break;
            case 2: filterAlbumTab(query);  break;
            default: filterSongTab(query);  break;
        }
    }

    private void filterSongTab(String query) {
        List<Song> result = new ArrayList<>();
        for (Song s : masterList) {
            if (matchesSong(s, query)) result.add(s);
        }
        songList.clear();
        songList.addAll(result);
        songAdapter.notifyDataSetChanged();
        updateSongCount();
    }

    private void filterSongByArtist(String artistName) {
        List<Song> result = new ArrayList<>();
        for (Song s : masterList) {
            String name = s.getArtist() != null ? s.getArtist().getName() : "";
            if (artistName.equalsIgnoreCase(name)) result.add(s);
        }
        songList.clear();
        songList.addAll(result);
        songAdapter.notifyDataSetChanged();
        updateSongCount();
    }

    private void filterSongByAlbum(String albumTitle) {
        List<Song> result = new ArrayList<>();
        for (Song s : masterList) {
            if (s.getAlbum() != null
                    && albumTitle.equalsIgnoreCase(s.getAlbum().getTitle())) {
                result.add(s);
            }
        }
        songList.clear();
        songList.addAll(result);
        songAdapter.notifyDataSetChanged();
        updateSongCount();
    }

    private void filterArtistTab(String query) {
        List<ArtistItem> all    = buildArtistItems(masterList);
        List<ArtistItem> result = new ArrayList<>();
        for (ArtistItem a : all) {
            if (query.isEmpty() || a.getName().toLowerCase().contains(query.toLowerCase()))
                result.add(a);
        }
        artistAdapter.updateList(result);
        if (binding != null) binding.tvSongCount.setText(result.size() + " artists");
    }

    private void filterAlbumTab(String query) {
        List<AlbumItem> all    = buildAlbumItems(masterList);
        List<AlbumItem> result = new ArrayList<>();
        for (AlbumItem a : all) {
            if (query.isEmpty()
                    || a.getTitle().toLowerCase().contains(query.toLowerCase())
                    || a.getArtistName().toLowerCase().contains(query.toLowerCase()))
                result.add(a);
        }
        albumAdapter.updateList(result);
        if (binding != null) binding.tvSongCount.setText(result.size() + " albums");
    }

    private boolean matchesSong(Song s, String query) {
        if (query.isEmpty()) return true;
        String q            = query.toLowerCase();
        boolean titleMatch  = s.getTitle().toLowerCase().contains(q);
        boolean artistMatch = s.getArtist() != null
                && s.getArtist().getName().toLowerCase().contains(q);
        return titleMatch || artistMatch;
    }

    // ------------------------------------------------------------------ //
    //  Networking
    // ------------------------------------------------------------------ //

    private void fetchFromApi(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutPlayAll.setVisibility(View.GONE);
        binding.bannerOffline.setVisibility(View.GONE);

        RetrofitInstance.getApi().searchSongs(query).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeezerResponse> call,
                                   @NonNull Response<DeezerResponse> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getData() != null
                        && !response.body().getData().isEmpty()) {

                    masterList.clear();
                    masterList.addAll(response.body().getData());

                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.layoutPlayAll.setVisibility(View.VISIBLE);

                    switch (currentTab) {
                        case 1: showArtistTab(); break;
                        case 2: showAlbumTab();  break;
                        default: showSongTab();  break;
                    }
                } else {
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.tvError.setText(
                            "Error " + response.code() + ": " + R.string.msg_not_found);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeezerResponse> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                showOfflineFallback();
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Offline fallback
    // ------------------------------------------------------------------ //

    private void showOfflineFallback() {
        if (binding == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutPlayAll.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<SongEntity> favorites = repository.getAllSongsSync();
            List<Song> converted = new ArrayList<>();
            for (SongEntity e : favorites) converted.add(entityToSong(e));

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                if (converted.isEmpty()) {
                    // Tidak ada data offline sama sekali → tampilkan error + btnRetry
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.tvError.setText(getString(R.string.msg_offline_no_data));
                } else {
                    // Ada data favorit → tampilkan list + banner offline
                    // btnHistory di search bar sudah selalu terlihat dan siap di-klik untuk retry
                    masterList.clear();
                    masterList.addAll(converted);

                    binding.bannerOffline.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.layoutPlayAll.setVisibility(View.VISIBLE);

                    switch (currentTab) {
                        case 1: showArtistTab(); break;
                        case 2: showAlbumTab();  break;
                        default: showSongTab();  break;
                    }
                }
            });
        });
    }

    private Song entityToSong(SongEntity e) {
        String json = "{\"id\":"       + e.getId()
                + ",\"title\":\""     + esc(e.getTitle())   + "\""
                + ",\"preview\":\""   + esc(e.getPlaybackPath()) + "\""
                + ",\"artist\":{\"name\":\"" + esc(e.getArtist()) + "\"}"
                + ",\"album\":{"
                +   "\"title\":\"Saved Song\","
                +   "\"cover_medium\":\"" + esc(e.getCover()) + "\""
                + "}}";
        return new com.google.gson.Gson().fromJson(json, Song.class);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void updateSongCount() {
        if (binding == null) return;
        int count = songList.size();
        binding.tvSongCount.setText(
                getResources().getQuantityString(R.plurals.song_count, count, count));
    }
}