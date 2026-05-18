package com.example.mymusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mymusic.activity.DetailActivity;
import com.example.mymusic.adapter.FavoriteAdapter;
import com.example.mymusic.database.AppDatabase;
import com.example.mymusic.database.SongEntity;
import com.example.mymusic.databinding.FragmentFavoriteBinding;
import com.example.mymusic.repository.SongRepository;
import com.example.mymusic.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private FavoriteAdapter         adapter;
    private SongRepository          repository;

    private final List<SongEntity> favoriteList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new SongRepository(
                AppDatabase.getInstance(requireContext()),
                requireContext()
        );

        setupRecyclerView();
        observeFavorites();
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter(
                favoriteList,
                song -> {
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    intent.putExtra(Constants.EXTRA_ID,      song.getId());
                    intent.putExtra(Constants.EXTRA_TITLE,   song.getTitle());
                    intent.putExtra(Constants.EXTRA_ARTIST,  song.getArtist());
                    intent.putExtra(Constants.EXTRA_COVER,   song.getCover());
                    intent.putExtra(Constants.EXTRA_PREVIEW, song.getPlaybackPath());
                    startActivity(intent);
                }
        );

        binding.recyclerFavorite.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.recyclerFavorite.setAdapter(adapter);
    }

    private void observeFavorites() {
        repository.getAllSongs().observe(getViewLifecycleOwner(), songs -> {
            updateList(songs);

            if (songs == null || songs.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.recyclerFavorite.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.recyclerFavorite.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateList(List<SongEntity> newList) {
        if (newList == null) return;
        int oldSize = favoriteList.size();
        favoriteList.clear();
        if (oldSize > 0) adapter.notifyItemRangeRemoved(0, oldSize);
        favoriteList.addAll(newList);
        if (!newList.isEmpty()) adapter.notifyItemRangeInserted(0, newList.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}