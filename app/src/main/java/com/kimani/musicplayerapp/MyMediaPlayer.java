package com.kimani.musicplayerapp;

import android.media.MediaPlayer;
import java.util.ArrayList;

/**
 * MyMediaPlayer is a singleton class used to manage the local Android MediaPlayer instance.
 * It maintains the current playlist and the index of the song being played to ensure 
 * consistent playback across different activities.
 */
public class MyMediaPlayer {

    // A static instance of MediaPlayer to ensure only one instance exists throughout the app life cycle
    private static MediaPlayer instance;

    /**
     * A static list that holds the current collection of songs (AudioModel) selected for playback.
     */
    public static ArrayList<AudioModel> playlist = new ArrayList<>();

    /**
     * The index of the currently playing song within the playlist.
     * Initialized to -1 to indicate no song is currently selected.
     */
    public static int currentIndex = -1;

    /**
     * Returns the single instance of MediaPlayer. 
     * If the instance doesn't exist, it initializes a new one.
     * 
     * @return The singleton MediaPlayer instance.
     */
    public static MediaPlayer getInstance() {
        if (instance == null) {
            instance = new MediaPlayer();
        }
        return instance;
    }
}
