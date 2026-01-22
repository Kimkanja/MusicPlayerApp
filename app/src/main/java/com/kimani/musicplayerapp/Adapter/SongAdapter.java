package com.kimani.musicplayerapp.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kimani.musicplayerapp.R;
import com.kimani.musicplayerapp.databinding.ItemSongBinding;
import com.kimani.musicplayerapp.models.TrackInfo;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewholder> {

    private final List<TrackInfo> songs;
    private final OnItemClickListener listener;

    // Interface to handle clicks back in SearchActivity
    public interface OnItemClickListener {
        void onItemClick(TrackInfo track);
    }

    public SongAdapter(List<TrackInfo> songs, OnItemClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Using ViewBinding as seen in your previous code
        ItemSongBinding binding = ItemSongBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SongViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewholder holder, int position) {
        TrackInfo track = songs.get(position);

        // Set Text Data
        holder.binding.textTitle.setText(track.getTitle());
        holder.binding.textArtist.setText(track.getSubtitle());

        // Animation
        holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.getContext(),
                        R.anim.scroll_recyclerview)
        );

        // Load Cover Art from URL using Glide
        Glide.with(holder.binding.getRoot().getContext())
                .load(track.getCoverUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_music_note_24)
                .error(R.drawable.ic_music_note_24)
                .into(holder.binding.imageAlbumArt);

        // Click Listener for Playback
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(track);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongViewholder extends RecyclerView.ViewHolder {
        final ItemSongBinding binding;

        public SongViewholder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}