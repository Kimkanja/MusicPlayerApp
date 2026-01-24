package com.kimani.musicplayerapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import com.kimani.musicplayerapp.MyExoplayer;
import com.kimani.musicplayerapp.OnlinePlayerActivity;
import com.kimani.musicplayerapp.databinding.SectionSongListRecyclerRowBinding;
import com.kimani.musicplayerapp.models.SongModel;

/**
 * SectionSongListAdapter displays a list of songs within a specific section (e.g., "Trending" or "Recommended").
 * It takes a list of song IDs and fetches the full song metadata from Firebase Firestore asynchronously.
 */
public class SectionSongListAdapter extends RecyclerView.Adapter<SectionSongListAdapter.MyViewHolder> {

    private final List<String> songIdList;

    /**
     * Constructor for SectionSongListAdapter.
     * @param songIdList List of unique song IDs to be fetched and displayed.
     */
    public SectionSongListAdapter(List<String> songIdList) {
        this.songIdList = songIdList;
    }

    /**
     * ViewHolder class that handles the view for each song item in the section.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final SectionSongListRecyclerRowBinding binding;

        public MyViewHolder(SectionSongListRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Fetches song data from Firestore using the provided songId and binds it to the views.
         * @param songId The ID of the song to retrieve.
         */
        public void bindData(String songId) {
            FirebaseFirestore.getInstance().collection("songs")
                    .document(songId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            // Convert the Firestore document into a SongModel object
                            SongModel song = documentSnapshot.toObject(SongModel.class);
                            if (song != null) {
                                // Set the song title and subtitle (artist)
                                binding.songTitleTextView.setText(song.getTitle());
                                binding.songSubtitleTextView.setText(song.getSubtitle());

                                // Load the song's cover art using Glide
                                Glide.with(binding.songCoverImageView.getContext())
                                        .load(song.getCoverUrl())
                                        .apply(new RequestOptions().transform(new RoundedCorners(32)))
                                        .into(binding.songCoverImageView);

                                // Setup click listener to start playback and open the OnlinePlayerActivity
                                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Context context = binding.getRoot().getContext();
                                        // Initialize playback using the singleton player manager
                                        MyExoplayer.startPlaying(context, song);
                                        context.startActivity(new Intent(context, OnlinePlayerActivity.class));
                                    }
                                });
                            }
                        }
                    });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the row layout using View Binding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SectionSongListRecyclerRowBinding binding = SectionSongListRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        // Return the size of the ID list
        return songIdList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Trigger data fetching and binding for the current position
        holder.bindData(songIdList.get(position));
    }
}
