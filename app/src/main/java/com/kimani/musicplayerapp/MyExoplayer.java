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

/**
 * MyExoplayer is a singleton-like wrapper for the Media3 ExoPlayer.
 * It manages online music playback, tracks the current song, and handles network-related interruptions.
 */
public class MyExoplayer {

    private static ExoPlayer exoPlayer = null;
    private static SongModel currentSong = null;
    private static Context currentContext = null; // Used for displaying dialogs

    // Private constructor to prevent instantiation
    private MyExoplayer() {}

    /**
     * @return The currently playing SongModel.
     */
    public static SongModel getCurrentSong() {
        return currentSong;
    }

    /**
     * @return The singleton instance of ExoPlayer.
     */
    public static ExoPlayer getInstance() {
        return exoPlayer;
    }

    /**
     * Starts playback for a given song. Initializes the player if it doesn't exist.
     *
     * @param context The context used to build the player and show dialogs.
     * @param song    The song to be played.
     */
    public static void startPlaying(@NonNull Context context, @NonNull SongModel song) {
        currentContext = context;
        
        // Initialize ExoPlayer if it's the first time
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
            exoPlayer.addListener(playerListener);
        }

        // Only load and play if the requested song is different from the current one
        if (!Objects.equals(currentSong, song)) {
            currentSong = song;
            updateCount(); // Increment play count in Firestore

            if (currentSong != null && currentSong.getUrl() != null) {
                MediaItem mediaItem = MediaItem.fromUri(currentSong.getUrl());
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
            }
        }
    }

    /**
     * Listener to monitor playback states and handle network drops.
     */
    private static final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            // If the player goes idle while it should be playing, check for internet connection
            if (playbackState == Player.STATE_IDLE && currentSong != null && exoPlayer != null && exoPlayer.getPlayWhenReady()) {
                 if (currentContext != null && !NetworkUtils.isNetworkAvailable(currentContext)) {
                     showNoInternetDialogDuringPlayback();
                 }
            }
        }
    };

    /**
     * Shows a dialog when internet is lost during active playback.
     */
    private static void showNoInternetDialogDuringPlayback() {
        if (currentContext == null || (exoPlayer != null && exoPlayer.isPlaying())) return;

        new AlertDialog.Builder(currentContext)
                .setTitle("No Internet Connection")
                .setMessage("Streaming stopped. Please check your connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                     // Attempt to resume if internet is back
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

    /**
     * Increments the play count for the current song in Firebase Firestore.
     */
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

                        // Update the 'count' field in the specific song document
                        FirebaseFirestore.getInstance().collection("songs")
                                .document(id)
                                .update(updates);
                    });
        }
    }
}
