package com.kimani.musicplayerapp;

import android.media.MediaPlayer;
import java.util.ArrayList;

public class MyMediaPlayer {

    // A static instance of MediaPlayer to ensure it's a singleton
    private static MediaPlayer instance;

    // A static list to hold the current playlist
    public static ArrayList<AudioModel> playlist = new ArrayList<>();

    // The index of the currently playing song in the playlist
    public static int currentIndex = -1;

    // A static method to get the single instance of MediaPlayer
    public static MediaPlayer getInstance() {
        if (instance == null) {
            instance = new MediaPlayer();
        }
        return instance;
    }
}
