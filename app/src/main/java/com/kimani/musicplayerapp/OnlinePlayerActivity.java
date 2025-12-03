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

public class OnlinePlayerActivity extends AppCompatActivity {

    // FIX 2: Declare the correct binding class variable
    private ActivityOnlinePlayerBinding binding;
    private ExoPlayer exoPlayer;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
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

        // FIX 3: Inflate the correct binding class
        binding = ActivityOnlinePlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null) {
            // These lines will now work because ActivityOnlinePlayerBinding is linked to the correct layout
            binding.songTitleTextView.setText(currentSong.getTitle());
            binding.songSubtitleTextView.setText(currentSong.getSubtitle());

            Glide.with(binding.songCoverImageView)
                    .load(currentSong.getCoverUrl())
                    .circleCrop()
                    .into(binding.songCoverImageView);

            Glide.with(binding.songGifImageView)
                    .load(R.drawable.media_playing)
                    .circleCrop()
                    .into(binding.songGifImageView);

            exoPlayer = MyExoplayer.getInstance();
            if (exoPlayer != null) {
                binding.playerView.setPlayer(exoPlayer);
                binding.playerView.showController();
                exoPlayer.addListener(playerListener);
                showGif(exoPlayer.isPlaying());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
        }
    }

    public void showGif(boolean show) {
        if (show) {
            binding.songGifImageView.setVisibility(View.VISIBLE);
        } else {
            binding.songGifImageView.setVisibility(View.INVISIBLE);
        }
    }
}
