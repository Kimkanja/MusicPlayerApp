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

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ListenableFuture<MediaController> mediaControllerFuture;
    private Handler handler;
    // --- FIX: This list is now generic to hold metadata, not a specific type ---
    private List<AudioModel> uiSongList = new ArrayList<>();
    private boolean isShuffle = false;
    private boolean isRepeat = false;

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
                            binding.waveformSeekBar.setProgressInPercentage(((float) currentPosition / duration));
                            binding.elapsedTimeText.setText(formatTime((int) (currentPosition / 1000)));
                            binding.songDurationtext.setText(formatTime((int) (duration / 1000)));
                        }
                        handler.postDelayed(this, 1000);
                    }
                } catch (Exception e) {
                    // This can happen if the controller is released, so it's safe to ignore here
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

        // --- FIX: Handle both Song and AudioModel lists ---
        // This logic converts whatever list we receive into a consistent `uiSongList` for the UI.
        ArrayList<Song> receivedSongList = getIntent().getParcelableArrayListExtra("songList");
        ArrayList<AudioModel> receivedAudioModelList = getIntent().getParcelableArrayListExtra("songList");

        if (receivedSongList != null && !receivedSongList.isEmpty() && receivedSongList.get(0) instanceof Song) {
            for (Song song : receivedSongList) {
                // Convert Song to AudioModel for UI consistency
                uiSongList.add(new AudioModel(song.getData(), song.getTitle(), "0", song.getArtist()));
            }
        } else if (receivedAudioModelList != null && !receivedAudioModelList.isEmpty()) {
            uiSongList.addAll(receivedAudioModelList);
        }
        // --- End of Fix ---

        if (uiSongList.isEmpty()) {
            Toast.makeText(this, "No Songs Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.waveformSeekBar.setWaveform(createWaveform(), true);
        setupControls();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // --- FIX: Pass the correct list type to the service ---
        Intent serviceIntent = new Intent(this, PlaybackService.class);
        ArrayList<Song> songListFromMain = getIntent().getParcelableArrayListExtra("songList");

        // We check if the intent came from MainActivity (contains `Song` objects)
        if (songListFromMain != null && !songListFromMain.isEmpty() && songListFromMain.get(0) instanceof Song) {
            serviceIntent.setAction("ACTION_START_FROM_MAIN");
            serviceIntent.putParcelableArrayListExtra("songList", songListFromMain);
        } else { // Otherwise, assume it's from PlaylistDetailsActivity (contains `AudioModel` objects)
            serviceIntent.setAction("ACTION_START_FROM_PLAYLIST");
            serviceIntent.putParcelableArrayListExtra("songList", getIntent().getParcelableArrayListExtra("songList"));
        }
        serviceIntent.putExtra("position", getIntent().getIntExtra("position", 0));
        startService(serviceIntent);
        // --- End of Fix ---

        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, PlaybackService.class));
        mediaControllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();

        mediaControllerFuture.addListener(() -> {
            try {
                MediaController mediaController = mediaControllerFuture.get();
                mediaController.addListener(playerListener);
                updateUIForCurrentSong(mediaController); // Update UI as soon as controller is ready
                handler.post(updateRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

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

    private void updateUIForCurrentSong(MediaController controller) {
        if (controller == null || controller.getMediaItemCount() == 0) {
            return;
        }

        try {
            int currentIndex = controller.getCurrentMediaItemIndex();

            if (uiSongList != null && !uiSongList.isEmpty() && currentIndex >= 0 && currentIndex < uiSongList.size()) {
                AudioModel song = uiSongList.get(currentIndex);

                binding.songTitleText.setText(song.getTitle());
                binding.songTitleText.setSelected(true);
                binding.songArtistText.setText(song.getArtist());
                binding.songArtistText.setSelected(true);
                setTitle(song.getTitle());

                binding.albumArtPlayerImage.setImageResource(R.drawable.ic_music_note_24);
                binding.albumArtBg.setImageResource(R.drawable.gradient_bg);

                updatePlayPauseButtonIcon();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private int[] createWaveform() {
        Random random = new Random(System.currentTimeMillis());
        int[] values = new int[50];
        for (int i = 0; i < values.length; i++) {
            values[i] = 5 + random.nextInt(50);
        }
        return values;
    }

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
            MediaController.releaseFuture(mediaControllerFuture);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
}
