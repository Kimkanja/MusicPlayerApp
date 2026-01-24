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
    private final OnItemLongClickListener longClickListener;

    // Interface for standard click (Play)
    public interface OnItemClickListener {
        void onItemClick(TrackInfo track);
    }

    // Interface for long click (Options Menu)
    public interface OnItemLongClickListener {
        void onItemLongClick(TrackInfo track);
    }

    // Updated Constructor to require both listeners
    public SongAdapter(List<TrackInfo> songs, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.songs = songs;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public SongViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        holder.binding.textTitle.setText(track.getTitle());
        holder.binding.textArtist.setText(track.getSubtitle());

        holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.getContext(),
                        R.anim.scroll_recyclerview)
        );

        Glide.with(holder.binding.getRoot().getContext())
                .load(track.getCoverUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_music_note_24)
                .error(R.drawable.ic_music_note_24)
                .into(holder.binding.imageAlbumArt);

        // Standard Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(track);
            }
        });

        // Long Click for BottomSheet logic
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(track);
                return true;
            }
            return false;
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

    // Add these methods inside SongAdapter.java

    public void updateData(List<TrackInfo> newSongs) {
        this.songs.clear();
        this.songs.addAll(newSongs);
        notifyDataSetChanged();
    }

    public void removeSong(int position) {
        if (position >= 0 && position < songs.size()) {
            songs.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, songs.size());
        }
    }
}