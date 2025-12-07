// C:/Users/PC/Desktop/MusicPlayerApp/kimani/MusicPlayerApp/app/src/main/java/com/kimani/musicplayerapp/PlayerActivity.java
package com.kimani.musicplayerapp;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.kimani.musicplayerapp.databinding.ActivityPlayerBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ListenableFuture<MediaController> mediaControllerFuture;
    private Handler handler;
    // FIX: Change songList to use AudioModel
    private List<AudioModel> songList = new ArrayList<>();
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
                    e.printStackTrace();
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

        // FIX: Receive ParcelableArrayList of AudioModel
        songList = getIntent().getParcelableArrayListExtra("songList");
        int initialPosition = getIntent().getIntExtra("position", 0);

        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "No Songs Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.waveformSeekBar.setWaveform(createWaveform(), true);

        // Start the service and send the playlist data
        Intent serviceIntent = new Intent(this, PlaybackService.class);
        serviceIntent.setAction("ACTION_START");
        // FIX: Send the ArrayList<AudioModel> to the service
        serviceIntent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
        serviceIntent.putExtra("position", initialPosition);
        startService(serviceIntent);

        setupControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, PlaybackService.class));
        mediaControllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();

        mediaControllerFuture.addListener(() -> {
            try {
                MediaController mediaController = mediaControllerFuture.get();
                mediaController.addListener(playerListener);
                updateUIForCurrentSong();
                handler.post(updateRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onMediaItemTransition(@androidx.annotation.Nullable MediaItem mediaItem, int reason) {
            updateUIForCurrentSong();
            handler.post(updateRunnable);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            updatePlayPauseButtonIcon();
            if(isPlaying){
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

    private void updateUIForCurrentSong() {
        if (mediaControllerFuture == null || !mediaControllerFuture.isDone()) return;
        try {
            MediaController controller = mediaControllerFuture.get();
            if (controller.getCurrentMediaItem() == null) return;

            int currentIndex = controller.getCurrentMediaItemIndex();
            // FIX: Get the song from the AudioModel list
            AudioModel song = songList.get(currentIndex);

            binding.songTitleText.setText(song.getTitle());
            binding.songTitleText.setSelected(true);
            binding.songArtistText.setText(song.getArtist());
            binding.songArtistText.setSelected(true);
            setTitle(song.getTitle());

            // FIX: AudioModel doesn't have album art, so we use a placeholder.
            // You can add albumId to AudioModel later if needed.
            binding.albumArtPlayerImage.setImageResource(R.drawable.ic_music_note_24);
            binding.albumArtBg.setImageResource(R.drawable.gradient_bg);

            updatePlayPauseButtonIcon();
        } catch (Exception e) { e.printStackTrace(); }
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
