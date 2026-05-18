package com.example.mymusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.database.SongEntity;
import com.example.mymusic.databinding.ItemFavoriteBinding;
import com.example.mymusic.listener.OnFavoriteClickListener;

import java.util.List;

public class FavoriteAdapter extends
        RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private final List<SongEntity>        favoriteList;
    private final OnFavoriteClickListener listener;

    public FavoriteAdapter(List<SongEntity> favoriteList,
                           OnFavoriteClickListener listener) {
        this.favoriteList = favoriteList;
        this.listener     = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {
        ItemFavoriteBinding binding = ItemFavoriteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(favoriteList.get(position));
    }

    @Override
    public int getItemCount() { return favoriteList.size(); }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {

        ItemFavoriteBinding binding;

        FavoriteViewHolder(ItemFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SongEntity song) {
            binding.tvTitle.setText(song.getTitle());
            binding.tvArtist.setText(song.getArtist());

            Glide.with(binding.getRoot().getContext())
                    .load(song.getCover())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .centerCrop()
                    .into(binding.imgSong);

            // ---- Badge offline (ikon kecil di sudut) ----
            // Tampilkan badge "tersimpan offline" jika file lokal sudah ada
            if (binding.ivOfflineBadge != null) {
                binding.ivOfflineBadge.setVisibility(
                        song.isDownloaded() ? View.VISIBLE : View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> listener.onFavoriteClick(song));
        }
    }
}