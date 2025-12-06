// Replace this file: app/src/main/java/com/kimani/musicplayerapp/Adapter/SongAdapter.java
package com.kimani.musicplayerapp.Adapter;

import android.content.ContentUris;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kimani.musicplayerapp.R;
import com.kimani.musicplayerapp.Song;
import com.kimani.musicplayerapp.databinding.ItemSongBinding;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewholder> {

    private final List<Song> songs;
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longClickListener;

    // ----- Click Listener -----
    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    // ----- Long Click Listener -----
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    // ----- Constructor -----
    public SongAdapter(List<Song> songs, OnItemClickListener listener,
                       OnItemLongClickListener longClickListener) {
        this.songs = songs;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    // ----- Delete Method -----
    public void deleteSong(int position) {
        if (position >= 0 && position < songs.size()) {
            songs.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public SongAdapter.SongViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSongBinding binding = ItemSongBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SongViewholder(binding, listener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongViewholder holder, int position) {
        Song song = songs.get(position);

        // Use the public getter methods to access the private fields
        holder.binding.textTitle.setText(song.getTitle());
        holder.binding.textArtist.setText(song.getArtist());

        holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.getContext(),
                        R.anim.scroll_recylerview)
        );

        // Use the public getter for albumId
        Uri albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                song.getAlbumId()
        );

        Glide.with(holder.binding.getRoot().getContext())
                .load(albumArtUri)
                .circleCrop()
                .placeholder(R.drawable.ic_music_note_24)
                .error(R.drawable.ic_music_note_24)
                .into(holder.binding.imageAlbumArt);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // ----- ViewHolder -----
    public static class SongViewholder extends RecyclerView.ViewHolder {

        final ItemSongBinding binding;

        public SongViewholder(ItemSongBinding binding,
                              OnItemClickListener clickListener,
                              OnItemLongClickListener longClickListener) {
            super(binding.getRoot());
            this.binding = binding;

            // Normal click
            binding.getRoot().setOnClickListener(view -> {
                if (clickListener != null) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        clickListener.OnItemClick(pos);
                    }
                }
            });

            // Long press
            binding.getRoot().setOnLongClickListener(view -> {
                if (longClickListener != null) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        longClickListener.onItemLongClick(pos);
                    }
                }
                return true;
            });
        }
    }
}
