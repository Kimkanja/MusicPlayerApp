package com.kimani.musicplayerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Import the MyMediaPlayer class to resolve the error
import com.kimani.musicplayerapp.MyMediaPlayer;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private final ArrayList<AudioModel> songsList;
    private final Context context;
    // --- NEW: Define a click listener interface ---
    private final OnSongClickListener songClickListener;

    // --- NEW: Interface for handling clicks ---
    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    // --- MODIFIED: Update the constructor to accept the listener ---
    public MusicAdapter(Context context, ArrayList<AudioModel> songsList, OnSongClickListener listener) {
        this.context = context;
        this.songsList = songsList;
        this.songClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel songData = songsList.get(position);
        holder.titleTextView.setText(songData.getTitle());

        if (MyMediaPlayer.currentIndex == position) {
            holder.titleTextView.setTextColor(Color.parseColor("#FF9800")); // Highlight the currently playing song
        } else {
            holder.titleTextView.setTextColor(Color.parseColor("#FFFFFF"));
        }

        // --- NEW: Set the click listener on the item view ---
        holder.itemView.setOnClickListener(v -> {
            if (songClickListener != null) {
                // Pass the clicked position to the activity
                songClickListener.onSongClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView iconImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.albumArtBg);
        }
    }
}
