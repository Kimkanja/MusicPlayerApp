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

/**
 * MusicAdapter is a RecyclerView adapter responsible for displaying a list of local audio files.
 * It handles item presentation and user interaction for selecting songs.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private final ArrayList<AudioModel> songsList;
    private final Context context;
    private final OnSongClickListener songClickListener;

    /**
     * Interface for handling click events on individual song items.
     */
    public interface OnSongClickListener {
        /**
         * Triggered when a song item is clicked.
         * @param position The adapter position of the clicked item.
         */
        void onSongClick(int position);
    }

    /**
     * Constructor for MusicAdapter.
     *
     * @param context   The application or activity context.
     * @param songsList The list of AudioModel objects to display.
     * @param listener  The listener to handle song selection events.
     */
    public MusicAdapter(Context context, ArrayList<AudioModel> songsList, OnSongClickListener listener) {
        this.context = context;
        this.songsList = songsList;
        this.songClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single item in the list
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel songData = songsList.get(position);
        holder.titleTextView.setText(songData.getTitle());

        // Highlight the currently playing song with a different color
        if (MyMediaPlayer.currentIndex == position) {
            holder.titleTextView.setTextColor(Color.parseColor("#FF9800")); // Orange for active song
        } else {
            holder.titleTextView.setTextColor(Color.parseColor("#FFFFFF")); // White for other songs
        }

        // Set the click listener on the item's root view
        holder.itemView.setOnClickListener(v -> {
            if (songClickListener != null) {
                // Pass the clicked position back to the host Activity/Fragment
                songClickListener.onSongClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    /**
     * ViewHolder class that holds references to the UI components of a song item.
     */
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
