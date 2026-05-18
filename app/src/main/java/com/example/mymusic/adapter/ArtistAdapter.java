package com.example.mymusic.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.mymusic.R;
import com.example.mymusic.databinding.ItemArtistBinding;
import com.example.mymusic.model.ArtistItem;

import java.util.List;

public class ArtistAdapter extends
        RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    public interface OnArtistClickListener {
        void onArtistClick(ArtistItem artist);
    }

    private final List<ArtistItem>      artistList;
    private final OnArtistClickListener listener;

    public ArtistAdapter(List<ArtistItem> artistList,
                         OnArtistClickListener listener) {
        this.artistList = artistList;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        ItemArtistBinding binding = ItemArtistBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ArtistViewHolder holder, int position) {
        holder.bind(artistList.get(position), listener);
    }

    @Override
    public int getItemCount() { return artistList.size(); }

    public void updateList(List<ArtistItem> newList) {
        int oldSize = artistList.size();
        artistList.clear();
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        artistList.addAll(newList);
        if (!newList.isEmpty()) notifyItemRangeInserted(0, newList.size());
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {

        private final ItemArtistBinding binding;

        public ArtistViewHolder(ItemArtistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ArtistItem artist, OnArtistClickListener listener) {
            binding.tvArtistName.setText(artist.getName());
            binding.tvSongCount.setText(artist.getSongCount() + " songs");

            Glide.with(binding.getRoot().getContext())
                    .load(artist.getPictureUrl())
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.imgArtist);

            binding.getRoot().setOnClickListener(
                    v -> listener.onArtistClick(artist));
        }
    }
}