package com.kimani.musicplayerapp;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.kimani.musicplayerapp.databinding.ActivityPlayerBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PlayerActivity is the main UI for controlling music playback.
 * It connects to a {@link PlaybackService} using a {@link MediaController} to manage playback state,
 * update the UI (seekbar, title, artist), and handle user interactions like play/pause, skip, shuffle, and repeat.
 */
public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ListenableFuture<MediaController> mediaControllerFuture;
    private Handler handler;
    
    // List used to populate the UI. It can hold data derived from different song models.
    private List<AudioModel> uiSongList = new ArrayList<>();
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    /**
     * Periodic task to update the seekbar and elapsed time text while music is playing.
     */
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaControllerFuture != null && mediaControllerFuture.isDone()) {
                try {
                    MediaController mediaController = mediaControllerFuture.get();
                    if (mediaController != null && mediaController.isPlaying()) {
                        long currentPosition = mediaController.getCurrentPosition();
                        long duration = mediaController.getDuration();
                        if (duration > 0) {
                            // Update seekbar percentage and time displays
                            binding.waveformSeekBar.setProgressInPercentage(((float) currentPosition / duration));
                            binding.elapsedTimeText.setText(formatTime((int) (currentPosition / 1000)));
                            binding.songDurationtext.setText(formatTime((int) (duration / 1000)));
                        }
                        // Re-schedule the update in 1 second
                        handler.postDelayed(this, 1000);
                    }
                } catch (Exception e) {
                    // Ignore exceptions if controller is released
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(Looper.getMainLooper());

        // Handle incoming song lists which might be of different types depending on the source activity
        ArrayList<Song> receivedSongList = getIntent().getParcelableArrayListExtra("songList");
        ArrayList<AudioModel> receivedAudioModelList = getIntent().getParcelableArrayListExtra("songList");

        if (receivedSongList != null && !receivedSongList.isEmpty() && receivedSongList.get(0) instanceof Song) {
            // Mapping Song objects (from MainActivity) to AudioModel for UI consistency
            for (Song song : receivedSongList) {
                uiSongList.add(new AudioModel(song.getData(), song.getTitle(), "0", song.getArtist()));
            }
        } else if (receivedAudioModelList != null && !receivedAudioModelList.isEmpty()) {
            // Directly using AudioModel objects (e.g., from PlaylistDetailsActivity)
            uiSongList.addAll(receivedAudioModelList);
        }

        if (uiSongList.isEmpty()) {
            Toast.makeText(this, "No Songs Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize waveform UI component with dummy data
        binding.waveformSeekBar.setWaveform(createWaveform(), true);
        setupControls();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start PlaybackService and pass the song list and starting position
        Intent serviceIntent = new Intent(this, PlaybackService.class);
        ArrayList<Song> songListFromMain = getIntent().getParcelableArrayListExtra("songList");

        // Set action based on data source to help the service handle the intent properly
        if (songListFromMain != null && !songListFromMain.isEmpty() && songListFromMain.get(0) instanceof Song) {
            serviceIntent.setAction("ACTION_START_FROM_MAIN");
            serviceIntent.putParcelableArrayListExtra("songList", songListFromMain);
        } else {
            serviceIntent.setAction("ACTION_START_FROM_PLAYLIST");
            serviceIntent.putParcelableArrayListExtra("songList", getIntent().getParcelableArrayListExtra("songList"));
        }
        serviceIntent.putExtra("position", getIntent().getIntExtra("position", 0));
        startService(serviceIntent);

        // Bind to the MediaSession in PlaybackService
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, PlaybackService.class));
        mediaControllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();

        mediaControllerFuture.addListener(() -> {
            try {
                MediaController mediaController = mediaControllerFuture.get();
                mediaController.addListener(playerListener);
                updateUIForCurrentSong(mediaController); // Initial UI update
                handler.post(updateRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Listener for player events like song transitions and play/pause state changes.
     */
    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onMediaItemTransition(@androidx.annotation.Nullable MediaItem mediaItem, int reason) {
            if (mediaControllerFuture.isDone()) {
                try {
                    updateUIForCurrentSong(mediaControllerFuture.get());
                    handler.post(updateRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            updatePlayPauseButtonIcon();
            if (isPlaying) {
                handler.post(updateRunnable);
            } else {
                handler.removeCallbacks(updateRunnable);
            }
        }
    };

    /**
     * Set up click listeners for all playback control buttons.
     */
    private void setupControls() {
        binding.playpauseBtn.setOnClickListener(v -> {
            if (mediaControllerFuture == null || !mediaControllerFuture.isDone()) return;
            try {
                MediaController controller = mediaControllerFuture.get();
                if (controller.isPlaying()) {
                    controller.pause();
                } else {
                    controller.play();
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        binding.nextBtn.setOnClickListener(v -> {
            if (mediaControllerFuture != null && mediaControllerFuture.isDone()) {
                try { mediaControllerFuture.get().seekToNextMediaItem(); }
                catch (Exception e) { e.printStackTrace(); }
            }
        });

        binding.previousBtn.setOnClickListener(v -> {
            if (mediaControllerFuture != null && mediaControllerFuture.isDone()) {
                try { mediaControllerFuture.get().seekToPreviousMediaItem(); }
                catch (Exception e) { e.printStackTrace(); }
            }
        });

        binding.shuffleBtn.setOnClickListener(v -> toggleShuffle());
        binding.repeatBtn.setOnClickListener(v -> toggleRepeat());
        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Toggles the repeat mode between REPEAT_MODE_ONE and REPEAT_MODE_OFF.
     */
    private void toggleRepeat() {
        if (mediaControllerFuture == null || !mediaControllerFuture.isDone()) return;
        try {
            MediaController controller = mediaControllerFuture.get();
            isRepeat = !isRepeat;
            controller.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
            binding.repeatBtn.setColorFilter(isRepeat ? getColor(R.color.orange) : getColor(android.R.color.white));
            Toast.makeText(this, isRepeat ? "Repeat ON" : "Repeat OFF", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Toggles shuffle mode in the media controller.
     */
    private void toggleShuffle() {
        if (mediaControllerFuture == null || !mediaControllerFuture.isDone()) return;
        try {
            MediaController controller = mediaControllerFuture.get();
            isShuffle = !isShuffle;
            controller.setShuffleModeEnabled(isShuffle);
            binding.shuffleBtn.setColorFilter(isShuffle ? getColor(R.color.orange) : getColor(android.R.color.white));
            Toast.makeText(this, isShuffle ? "Shuffle ON" : "Shuffle OFF", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Updates the UI elements (text views, images) with information about the currently playing song.
     * @param controller The MediaController instance.
     */
    private void updateUIForCurrentSong(MediaController controller) {
        if (controller == null || controller.getMediaItemCount() == 0) {
            return;
        }

        try {
            int currentIndex = controller.getCurrentMediaItemIndex();

            if (uiSongList != null && !uiSongList.isEmpty() && currentIndex >= 0 && currentIndex < uiSongList.size()) {
                AudioModel song = uiSongList.get(currentIndex);

                binding.songTitleText.setText(song.getTitle());
                binding.songTitleText.setSelected(true); // Enables marquee effect if text overflows
                binding.songArtistText.setText(song.getArtist());
                binding.songArtistText.setSelected(true);
                setTitle(song.getTitle());

                // Static placeholders for artwork; could be replaced with dynamic loading
                binding.albumArtPlayerImage.setImageResource(R.drawable.ic_music_note_24);
                binding.albumArtBg.setImageResource(R.drawable.gradient_bg);

                updatePlayPauseButtonIcon();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts seconds into a mm:ss format string.
     */
    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * Generates a random set of values to simulate a waveform for the seekbar.
     */
    private int[] createWaveform() {
        Random random = new Random(System.currentTimeMillis());
        int[] values = new int[50];
        for (int i = 0; i < values.length; i++) {
            values[i] = 5 + random.nextInt(50);
        }
        return values;
    }

    /**
     * Updates the play/pause button icon based on whether the player is currently playing.
     */
    private void updatePlayPauseButtonIcon() {
        if (mediaControllerFuture == null || !mediaControllerFuture.isDone()) return;
        try {
            binding.playpauseBtn.setImageResource(
                    mediaControllerFuture.get().isPlaying() ? R.drawable.icon_pause_24 : R.drawable.icon_play_24
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaControllerFuture != null) {
            mediaControllerFuture.addListener(() -> {
                try {
                    mediaControllerFuture.get().removeListener(playerListener);
                } catch (Exception e) {
                    // Ignore
                }
            }, MoreExecutors.directExecutor());
            // Release the MediaController when activity is stopped
            MediaController.releaseFuture(mediaControllerFuture);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            // Stop UI updates to prevent memory leaks
            handler.removeCallbacks(updateRunnable);
        }
    }
}
