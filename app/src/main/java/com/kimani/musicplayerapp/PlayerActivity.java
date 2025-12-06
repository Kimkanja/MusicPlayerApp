package com.kimani.musicplayerapp;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

// Media3 Imports (THE FIX)
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.frolo.waveformseekbar.WaveformSeekBar;
import com.kimani.musicplayerapp.databinding.ActivityPlayerBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    private ExoPlayer player;
    private Handler handler;
    private List<Song> songList = new ArrayList<>();
    private List<Song> shuffledList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;


    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if(player != null && player.isPlaying()){
                long currentPosition = player.getCurrentPosition();
                long duration = player.getDuration();
                if (duration > 0) {
                    float progressPercent = ((float) currentPosition / duration);
                    binding.waveformSeekBar.setProgressInPercentage(progressPercent);
                    binding.elapsedTimeText.setText(formatTime((int) (currentPosition / 1000)));
                    binding.songDurationtext.setText(formatTime((int) (duration / 1000)));
                }
                handler.postDelayed(this, 1000);
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

        songList = getIntent().getParcelableArrayListExtra("songList");
        currentIndex = getIntent().getIntExtra("position", 0);

        if(songList == null || songList.isEmpty()){
            Toast.makeText(this, "No Songs Found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        shuffledList = new ArrayList<>(songList);
        binding.waveformSeekBar.setWaveform(createWaveform(), true);

        initPlayerWithSong(currentIndex);
        setupControls();
    }

    private void setupControls() {
        binding.playpauseBtn.setOnClickListener(v -> togglePlayPause());
        binding.nextBtn.setOnClickListener(v -> playNext());
        binding.previousBtn.setOnClickListener(v -> playPrevious());
        binding.shuffleBtn.setOnClickListener(v -> toggleShuffle());
        binding.repeatBtn.setOnClickListener(v -> toggleRepeat());
        binding.backBtn.setOnClickListener(v -> toggleBackBtn());
        binding.waveformSeekBar.setCallback(new WaveformSeekBar.Callback() {

            @Override
            public void onProgressChanged(WaveformSeekBar seekBar, float percent, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long seekPos = (long) (percent * duration);
                    player.seekTo(seekPos);
                    binding.elapsedTimeText.setText(formatTime((int) (seekPos / 1000)));
                }
            }

            @Override
            public void onStartTrackingTouch(WaveformSeekBar seekBar) {
                if (handler != null) {
                    handler.removeCallbacks(updateRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(WaveformSeekBar seekBar) {
                if (handler != null) {
                    handler.postDelayed(updateRunnable, 0);
                }
            }
        });
    }

    private void toggleBackBtn() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        if (player != null) {
            player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        }
        binding.repeatBtn.setColorFilter(isRepeat ? getResources().getColor(R.color.orange, getTheme()) : getResources().getColor(android.R.color.white, getTheme()));
        Toast.makeText(this, isRepeat ? "Repeat ON" : "Repeat OFF", Toast.LENGTH_SHORT).show();
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {
            Collections.shuffle(shuffledList);
            binding.shuffleBtn.setColorFilter(getResources().getColor(R.color.orange, getTheme()));
        } else {
            Song currentSong = shuffledList.get(currentIndex);
            shuffledList = new ArrayList<>(songList);
            currentIndex = shuffledList.indexOf(currentSong);
            binding.shuffleBtn.clearColorFilter();
        }
        Toast.makeText(this, isShuffle ? "Shuffle ON" : "Shuffle OFF", Toast.LENGTH_SHORT).show();
    }

    private void playPrevious() {
        int listSize = isShuffle ? shuffledList.size() : songList.size();
        currentIndex = (currentIndex - 1 + listSize) % listSize;
        initPlayerWithSong(currentIndex);
    }

    private void togglePlayPause() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.pause();
            handler.removeCallbacks(updateRunnable);
        } else {
            player.play();
            handler.postDelayed(updateRunnable, 0);
        }
        updatePlayPauseButtonIcon();
    }


    private void initPlayerWithSong(int index) {
        if (songList.isEmpty()) return;

        Song song = isShuffle ? shuffledList.get(index) : songList.get(index);

        if (player != null) player.release();
        player = new ExoPlayer.Builder(this).build();
        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updatePlayPauseButtonIcon();
                if (playbackState == Player.STATE_READY) {
                    binding.songDurationtext.setText(formatTime((int) (player.getDuration() / 1000)));
                    handler.postDelayed(updateRunnable, 0);
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Error playing media: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        player.setMediaItem(MediaItem.fromUri(song.getData()));
        player.prepare();
        player.play();

        updatePlayPauseButtonIcon();
        updateUI(song);
    }

    private void updateUI(Song song) {
        binding.songTitleText.setText(song.getTitle());
        binding.songTitleText.setSelected(true);

        binding.songArtistText.setText(song.getArtist());
        binding.songArtistText.setSelected(true);

        setTitle(song.getTitle());

        Uri albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), song.getAlbumId());

        if (hasAlbumArt(albumArtUri)) {
            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(binding.albumArtPlayerImage);

            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                    .placeholder(R.drawable.gradient_bg)
                    .error(R.drawable.gradient_bg)
                    .into(binding.albumArtBg);
        } else {
            binding.albumArtPlayerImage.setImageResource(R.drawable.ic_music_note_24);
            binding.albumArtBg.setImageResource(R.drawable.gradient_bg);
        }
    }


    private boolean hasAlbumArt(Uri albumArtUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(albumArtUri)) {
            return inputStream != null;
        } catch (Exception e) {
            return false;
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
        if (player == null) return;
        binding.playpauseBtn.setImageResource(
                player.isPlaying() ? R.drawable.icon_pause_24 : R.drawable.icon_play_24
        );
    }

    private void playNext() {
        if (songList.isEmpty()) return;
        int listSize = isShuffle ? shuffledList.size() : songList.size();
        currentIndex = (currentIndex + 1) % listSize;
        initPlayerWithSong(currentIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(updateRunnable);
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
