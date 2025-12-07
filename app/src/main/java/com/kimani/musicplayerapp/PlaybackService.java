package com.kimani.musicplayerapp;

import android.content.Intent;
import android.net.Uri;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaybackService extends MediaLibraryService {

    private MediaLibrarySession mediaLibrarySession;
    private ExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new MediaLibrarySession.Callback() {
            /**
             * This method signature has been updated to match the newer versions of the androidx.media3 library.
             * It now includes a MediaSession.ControllerInfo parameter.
             */
            @Override
            public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser, // This parameter was missing
                    LibraryParams params) {

                // For now, we'll return a simple root item, but you can customize this.
                // It is good practice to return a browsable root item.
                MediaItem rootItem = new MediaItem.Builder()
                        .setMediaId("root")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Music Library")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();
                return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params));
            }
        }).build();
    }

    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_START".equals(intent.getAction())) {
            ArrayList<AudioModel> songList = intent.getParcelableArrayListExtra("songList");
            int position = intent.getIntExtra("position", 0);

            if (songList != null && !songList.isEmpty()) {
                List<MediaItem> mediaItems = new ArrayList<>();
                for (AudioModel song : songList) {
                    MediaItem mediaItem = new MediaItem.Builder()
                            .setUri(Uri.parse(song.getPath()))
                            .setMediaMetadata(new MediaMetadata.Builder()
                                    .setTitle(song.getTitle())
                                    .setArtist(song.getArtist())
                                    .build())
                            .build();
                    mediaItems.add(mediaItem);
                }
                player.setMediaItems(mediaItems, position, 0);
                player.prepare();
                player.play();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        if (mediaLibrarySession != null) {
            mediaLibrarySession.release();
        }
        super.onDestroy();
    }
}
