package com.kimani.musicplayerapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;

// FIX 1: Import the correct binding class
import com.kimani.musicplayerapp.databinding.ActivityOnlinePlayerBinding;
import com.kimani.musicplayerapp.models.SongModel;

/**
 * OnlinePlayerActivity is responsible for displaying the full-screen music player UI
 * for online songs. it integrates with the shared MyExoplayer instance to control
 * playback and update UI components like titles, cover art, and progress.
 */
public class OnlinePlayerActivity extends AppCompatActivity {

    // View binding instance for the activity layout
    private ActivityOnlinePlayerBinding binding;
    
    // Reference to the shared ExoPlayer instance
    private ExoPlayer exoPlayer;

    /**
     * Listener to track ExoPlayer events, specifically to update the UI
     * when the playback state changes (e.g., showing/hiding a playing animation).
     */
    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            // Toggle the visual "playing" indicator based on player state
            if (playbackState == Player.STATE_READY && exoPlayer.isPlaying()) {
                showGif(true);
            } else {
                showGif(false);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using view binding
        binding = ActivityOnlinePlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the current song metadata from the shared player manager
        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null) {
            // Update UI components with song details
            binding.songTitleTextView.setText(currentSong.getTitle());
            binding.songSubtitleTextView.setText(currentSong.getSubtitle());

            // Load the album cover image with a circular crop
            Glide.with(binding.songCoverImageView)
                    .load(currentSong.getCoverUrl())
                    .circleCrop()
                    .into(binding.songCoverImageView);

            // Load the animated "playing" GIF
            Glide.with(binding.songGifImageView)
                    .load(R.drawable.media_playing)
                    .circleCrop()
                    .into(binding.songGifImageView);

            // Connect the layout's PlayerView to the shared ExoPlayer instance
            exoPlayer = MyExoplayer.getInstance();
            if (exoPlayer != null) {
                binding.playerView.setPlayer(exoPlayer);
                binding.playerView.showController(); // Ensure transport controls are visible
                exoPlayer.addListener(playerListener);
                
                // Initial check for play/pause state
                showGif(exoPlayer.isPlaying());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener to prevent memory leaks or crashes when the activity is destroyed
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
        }
    }

    /**
     * Toggles the visibility of the "now playing" GIF animation.
     * 
     * @param show True to make the GIF visible, false to hide it.
     */
    public void showGif(boolean show) {
        if (show) {
            binding.songGifImageView.setVisibility(View.VISIBLE);
        } else {
            binding.songGifImageView.setVisibility(View.INVISIBLE);
        }
    }
}
