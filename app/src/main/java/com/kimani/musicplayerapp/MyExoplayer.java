package com.kimani.musicplayerapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kimani.musicplayerapp.models.SongModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyExoplayer {

    private static ExoPlayer exoPlayer = null;
    private static SongModel currentSong = null;
    private static Context currentContext = null; // To show dialogs


    private MyExoplayer() {}

    public static SongModel getCurrentSong() {
        return currentSong;
    }

    public static ExoPlayer getInstance() {
        return exoPlayer;
    }

    public static void startPlaying(@NonNull Context context, @NonNull SongModel song) {
        currentContext = context; // Store context
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
            exoPlayer.addListener(playerListener); // Add the listener
        }

        // Only reset and play if it's a new song
        if (!Objects.equals(currentSong, song)) {
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

    // Listener to detect playback state changes
    private static final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            // This logic requires a NetworkUtils class that you have not provided.
            // Assuming it exists and works as intended.
            if (playbackState == Player.STATE_IDLE && currentSong != null && exoPlayer != null && exoPlayer.getPlayWhenReady()) {
                 if (currentContext != null && !NetworkUtils.isNetworkAvailable(currentContext)) {
                     showNoInternetDialogDuringPlayback();
                 }
                // }
            }
        }
    };

    private static void showNoInternetDialogDuringPlayback() {
        if (currentContext == null || (exoPlayer != null && exoPlayer.isPlaying())) return;

        new AlertDialog.Builder(currentContext)
                .setTitle("No Internet Connection")
                .setMessage("Streaming stopped. Please check your connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                     if (exoPlayer != null && NetworkUtils.isNetworkAvailable(currentContext)) {
                        exoPlayer.prepare();
                         exoPlayer.play();
                     } else {
                         showNoInternetDialogDuringPlayback();
                     }
                })
                .setNegativeButton("Offline Mode", (dialog, which) -> {
                    if(exoPlayer != null) {
                        exoPlayer.stop();
                    }
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    public static void updateCount() {
        if (currentSong != null && currentSong.getId() != null) {
            String id = currentSong.getId();
            FirebaseFirestore.getInstance().collection("songs")
                    .document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
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
                    });
        }
    }
}
