package com.kimani.musicplayerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for the RecyclerView in SongPickerActivity.
 * Displays a list of songs with checkboxes to allow multi-selection.
 */
public class SongPickerAdapter extends RecyclerView.Adapter<SongPickerAdapter.ViewHolder> {

    private final Context context;
    private final List<AudioModel> songsList;
    private final OnSongSelectedListener listener;

    /**
     * Interface to handle song selection events.
     */
    public interface OnSongSelectedListener {
        /**
         * Called when a song is selected or deselected.
         * @param song The song that was clicked.
         */
        void onSongSelected(AudioModel song);
    }

    /**
     * Constructs the adapter.
     * @param context The activity context.
     * @param songsList The list of all available songs to display.
     * @param listener Callback for selection changes.
     */
    public SongPickerAdapter(Context context, List<AudioModel> songsList, OnSongSelectedListener listener) {
        this.context = context;
        this.songsList = songsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each song item in the picker
        View view = LayoutInflater.from(context).inflate(R.layout.song_picker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel song = songsList.get(position);
        holder.titleTextView.setText(song.getTitle());
        holder.artistTextView.setText(song.getArtist());
        
        // Note: In a production app, you'd track selection state in AudioModel or a separate Set
        // to ensure the checkbox remains checked when scrolling.
        holder.checkBox.setChecked(false); 

        // Clicking the entire row toggles the checkbox and triggers the listener
        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            listener.onSongSelected(song);
        });

        // Directly clicking the checkbox also triggers the listener
        holder.checkBox.setOnClickListener(v -> listener.onSongSelected(song));
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    /**
     * ViewHolder class that holds references to the UI components for each song item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView artistTextView;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            artistTextView = itemView.findViewById(R.id.music_artist_text);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
