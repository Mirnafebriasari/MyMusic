package com.example.mymusic.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.databinding.ItemAlbumBinding;
import com.example.mymusic.model.AlbumItem;

import java.util.List;

public class AlbumAdapter extends
        RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    public interface OnAlbumClickListener {
        void onAlbumClick(AlbumItem album);
    }

    private final List<AlbumItem>      albumList;
    private final OnAlbumClickListener listener;

    public AlbumAdapter(List<AlbumItem> albumList,
                        OnAlbumClickListener listener) {
        this.albumList = albumList;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        ItemAlbumBinding binding = ItemAlbumBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AlbumViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AlbumViewHolder holder, int position) {
        holder.bind(albumList.get(position), listener);
    }

    @Override
    public int getItemCount() { return albumList.size(); }

    public void updateList(List<AlbumItem> newList) {
        int oldSize = albumList.size();
        albumList.clear();
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        albumList.addAll(newList);
        if (!newList.isEmpty()) notifyItemRangeInserted(0, newList.size());
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        private final ItemAlbumBinding binding;

        public AlbumViewHolder(ItemAlbumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AlbumItem album, OnAlbumClickListener listener) {
            binding.tvAlbumTitle.setText(album.getTitle());
            binding.tvAlbumArtist.setText(album.getArtistName());
            binding.tvAlbumSongCount.setText(album.getSongCount() + " songs");

            Glide.with(binding.getRoot().getContext())
                    .load(album.getCoverUrl())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .centerCrop()
                    .into(binding.imgAlbumCover);

            binding.getRoot().setOnClickListener(
                    v -> listener.onAlbumClick(album));
        }
    }
}