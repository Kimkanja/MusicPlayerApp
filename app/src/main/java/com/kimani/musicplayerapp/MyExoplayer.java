package com.kimani.musicplayerapp;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.kimani.musicplayerapp.models.SongModel;

public class MyExoplayer {

    private static ExoPlayer exoPlayer = null;
    private static SongModel currentSong = null;

    // Private constructor to prevent instantiation
    private MyExoplayer() {}

    @Nullable
    public static SongModel getCurrentSong() {
        return currentSong;
    }

    @Nullable
    public static ExoPlayer getInstance() {
        return exoPlayer;
    }

    public static void startPlaying(@NonNull Context context, @NonNull SongModel song) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
        }

        if (!Objects.equals(currentSong, song)) {
            // It's a new song, so start playing
            currentSong = song;
            updateCount();

            if (currentSong != null && currentSong.getUrl() != null) {
                MediaItem mediaItem = MediaItem.fromUri(currentSong.getUrl());
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
            }
        }
    }

    public static void updateCount() {
        if (currentSong != null && currentSong.getId() != null) {
            String id = currentSong.getId();
            FirebaseFirestore.getInstance().collection("songs")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Long latestCount = documentSnapshot.getLong("count");
                            if (latestCount == null) {
                                latestCount = 1L;
                            } else {
                                latestCount = latestCount + 1;
                            }

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("count", latestCount);

                            FirebaseFirestore.getInstance().collection("songs")
                                    .document(id)
                                    .update(updates);
                        }
                    });
        }
    }
}
