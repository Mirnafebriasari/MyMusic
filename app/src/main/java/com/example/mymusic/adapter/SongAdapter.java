package com.example.mymusic.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.databinding.ItemSongBinding;
import com.example.mymusic.listener.OnSongClickListener;
import com.example.mymusic.model.Song;

import java.util.List;

public class SongAdapter extends
        RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songList;
    private final OnSongClickListener listener;

    public SongAdapter(List<Song> songList,
                       OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        ItemSongBinding binding = ItemSongBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new SongViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SongViewHolder holder, int position) {

        Song song = songList.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        ItemSongBinding binding;

        SongViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Song song) {

            // Title
            binding.tvTitle.setText(song.getTitle());

            // Artist
            if (song.getArtist() != null) {
                binding.tvArtist.setText(
                        song.getArtist().getName());
            } else {
                binding.tvArtist.setText("Unknown Artist");
            }

            // Cover image via Glide
            if (song.getAlbum() != null) {
                Glide.with(binding.getRoot().getContext())
                        .load(song.getAlbum().getCoverMedium())
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .centerCrop()
                        .into(binding.imgSong);
            } else {
                binding.imgSong.setImageResource(
                        R.drawable.logo);
            }

            // Klik item
            binding.getRoot().setOnClickListener(v ->
                    listener.onSongClick(song));
        }
    }
}