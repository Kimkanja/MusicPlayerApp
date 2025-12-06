// Replace this file: app/src/main/java/com/kimani/musicplayerapp/PlaybackService.java
package com.kimani.musicplayerapp;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata; // Make sure this is imported
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;


import java.util.ArrayList;
import java.util.List;

public class PlaybackService extends MediaSessionService {

    private MediaSession mediaSession;
    private ExoPlayer player;
    private List<Song> songList = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the player
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_LOCAL)
                .build();

        // Create a MediaSession
        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(getSingleTopActivity())
                .build();
    }

    // This is the activity that will be opened when the user clicks the notification.
    private PendingIntent getSingleTopActivity() {
        return PendingIntent.getActivity(
                this,
                0,
                new Intent(this, PlayerActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }


    // This method is called when PlayerActivity sends the song list and position
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if ("ACTION_START".equals(intent.getAction())) {
                ArrayList<Song> receivedList = intent.getParcelableArrayListExtra("songList");
                int position = intent.getIntExtra("position", 0);

                if (receivedList != null && !receivedList.isEmpty()) {
                    this.songList = receivedList;
                    List<MediaItem> mediaItems = new ArrayList<>();

                    // --- THIS IS THE FIX ---
                    // Build each MediaItem with its corresponding metadata, including Artwork URI
                    for (Song song : songList) {
                        // Create the URI for the album art
                        Uri artworkUri = ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"),
                                song.getAlbumId()
                        );

                        // Create metadata for the song
                        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                                .setTitle(song.getTitle())
                                .setArtist(song.getArtist())
                                .setArtworkUri(artworkUri) // <-- ADD THIS LINE
                                .build();

                        // Build a MediaItem and attach the metadata
                        MediaItem mediaItem = new MediaItem.Builder()
                                .setUri(song.getData())
                                .setMediaMetadata(mediaMetadata)
                                .build();

                        mediaItems.add(mediaItem);
                    }
                    // --- END OF FIX ---

                    player.setMediaItems(mediaItems, position, 0);
                    player.prepare();
                    player.play();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Stop playback and service when the user swipes the app away from recents
        if (player != null && !player.getPlayWhenReady()) {
            player.release();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
        }
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }
}
