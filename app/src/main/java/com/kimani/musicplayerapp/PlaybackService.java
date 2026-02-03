package com.kimani.musicplayerapp;
import android.app.PendingIntent;
import android.content.ContentUris;
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
import java.util.Objects;

/**
 * PlaybackService is a MediaLibraryService that handles background audio playback
 * using Media3 (ExoPlayer). It manages the media session and allows for 
 * external control via notifications and system-wide media controls.
 */
public class PlaybackService extends MediaLibraryService {

    private MediaLibrarySession mediaLibrarySession;
    private ExoPlayer player;
    //TRACK CURRENT PLAYBACK STATE
    public static int currentIndex = -1;
    private boolean hasInitializedQueue = false;
    public static boolean isRunning = false;
    private List<MediaItem> currentQueue = new ArrayList<>();




    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                currentIndex = player.getCurrentMediaItemIndex();
            }
        });


        // Initialize MediaLibrarySession with a callback to handle browser interactions
        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new MediaLibrarySession.Callback() {
            @Override
            public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                    MediaLibrarySession session,
                    MediaSession.ControllerInfo browser,
                    LibraryParams params) {
                // Define the root of the media library
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

        // Set the PendingIntent to open PlayerActivity when the user clicks on the notification
        Intent playerIntent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, playerIntent, PendingIntent.FLAG_IMMUTABLE);
        mediaLibrarySession.setSessionActivity(pendingIntent);
    }

    /**
     * Returns the current media library session to connecting controllers.
     */
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    /**
     * Handles service start commands, specifically starting playback from different sources
     * based on intent actions.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            int position = intent.getIntExtra("position", 0);
            List<MediaItem> mediaItems = new ArrayList<>();

            // Handle starting playback from a playlist (local AudioModel list)
            if (action.equals("ACTION_START_FROM_PLAYLIST")) {
                ArrayList<AudioModel> songList = intent.getParcelableArrayListExtra("songList");
                if (songList != null && !songList.isEmpty()) {
                    for (AudioModel song : songList) {

                        mediaItems.add(
                                new MediaItem.Builder()
                                        .setUri(Uri.parse(song.getPath()))
                                        .setMediaId(song.getPath())
                                        .setMediaMetadata(
                                                new MediaMetadata.Builder()
                                                        .setTitle(song.getTitle())
                                                        .setArtist(song.getArtist())
                                                        .build()
                                        )
                                        .build()
                        );
                    }
                }
            }

            // Handle starting playback from the main local song list
            else if (action.equals("ACTION_START_FROM_MAIN")) {
                ArrayList<Song> songList = intent.getParcelableArrayListExtra("songList");
                if (songList != null && !songList.isEmpty()) {
                    for (Song song : songList) {

                        Uri albumArtUri = ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"),
                                song.getAlbumId()
                        );

                        mediaItems.add(
                                new MediaItem.Builder()
                                        .setUri(Uri.parse(song.getData()))
                                        .setMediaId(song.getData())
                                        .setMediaMetadata(
                                                new MediaMetadata.Builder()
                                                        .setTitle(song.getTitle())
                                                        .setArtist(song.getArtist())
                                                        .setArtworkUri(albumArtUri)
                                                        .build()
                                        )
                                        .build()
                        );
                    }
                }
            }


            // If media items were parsed successfully, set them to the player and start
            if (!mediaItems.isEmpty()) {

                // CASE 1: First time ever → initialize queue
                if (!hasInitializedQueue) {

                    player.setMediaItems(mediaItems, position, 0);
                    player.prepare();
                    player.play();

                    hasInitializedQueue = true;
                    currentIndex = position;
                    return START_STICKY;
                }

                // CASE 2: User clicked a DIFFERENT song either from the main list or from the playlist (search, playlist, etc.)
                if (!mediaItems.equals(currentQueue)) {

                    player.setMediaItems(mediaItems, position, 0);
                    player.prepare();
                    player.play();

                    currentQueue.clear();
                    currentQueue.addAll(mediaItems);

                    currentIndex = position;

                }
                // CASE 3: Same list, different song
                else if (position != currentIndex) {

                    player.seekTo(position, 0);
                    player.play();

                    currentIndex = position;
                }
            }


        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // Release resources to avoid memory leaks
        if (player != null) {
            player.release();
        }
        if (mediaLibrarySession != null) {
            mediaLibrarySession.release();
        }
        super.onDestroy();
    }
}
