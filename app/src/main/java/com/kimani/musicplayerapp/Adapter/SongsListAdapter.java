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
import com.kimani.musicplayerapp.databinding.SongListItemRecyclerRowBinding;
import com.kimani.musicplayerapp.models.SongModel;

/**
 * SongsListAdapter is used to display a list of songs, typically within a category or playlist.
 * Similar to SectionSongListAdapter, it fetches song details from Firestore using their IDs.
 */
public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.MyViewHolder> {

    private final List<String> songIdList;

    /**
     * Constructor for SongsListAdapter.
     * @param songIdList List of song IDs to be retrieved and displayed.
     */
    public SongsListAdapter(List<String> songIdList) {
        this.songIdList = songIdList;
    }

    /**
     * ViewHolder class that manages the layout and data binding for individual song items.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final SongListItemRecyclerRowBinding binding;

        public MyViewHolder(SongListItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Fetches song metadata from Firebase Firestore and populates the item views.
         * Also sets up a click listener to play the song.
         * @param songId The Firestore document ID for the song.
         */
        public void bindData(String songId) {
            FirebaseFirestore.getInstance().collection("songs")
                    .document(songId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            // Map the Firestore document to a SongModel object
                            SongModel song = documentSnapshot.toObject(SongModel.class);
                            if (song != null) {
                                // Update text views with song information
                                binding.songTitleTextView.setText(song.getTitle());
                                binding.songSubtitleTextView.setText(song.getSubtitle());

                                // Load the song's cover art using Glide with rounded corners
                                Glide.with(binding.songCoverImageView.getContext())
                                        .load(song.getCoverUrl())
                                        .apply(new RequestOptions().transform(new RoundedCorners(32)))
                                        .into(binding.songCoverImageView);

                                // On item click, start the player and navigate to the online player screen
                                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Context context = binding.getRoot().getContext();
                                        // Use the MyExoplayer utility to manage playback state
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
        // Inflate the song item layout using View Binding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SongListItemRecyclerRowBinding binding = SongListItemRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        // Return the number of songs to be displayed
        return songIdList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Request data binding for the song at the current position
        holder.bindData(songIdList.get(position));
    }
}
