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

public class SongPickerAdapter extends RecyclerView.Adapter<SongPickerAdapter.ViewHolder> {

    private final Context context;
    private final List<AudioModel> songsList;
    private final OnSongSelectedListener listener;

    public interface OnSongSelectedListener {
        void onSongSelected(AudioModel song);
    }

    public SongPickerAdapter(Context context, List<AudioModel> songsList, OnSongSelectedListener listener) {
        this.context = context;
        this.songsList = songsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_picker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel song = songsList.get(position);
        holder.titleTextView.setText(song.getTitle());
        holder.artistTextView.setText(song.getArtist());
        holder.checkBox.setChecked(false); // Reset checkbox state

        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            listener.onSongSelected(song);
        });

        holder.checkBox.setOnClickListener(v -> listener.onSongSelected(song));
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

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
