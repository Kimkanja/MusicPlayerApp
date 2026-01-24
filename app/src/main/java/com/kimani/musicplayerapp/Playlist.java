package com.kimani.musicplayerapp;

/**
 * Data model representing a music playlist.
 * Stores basic information about a playlist, such as its name.
 */
public class Playlist {
    private String name;

    /**
     * Constructs a new Playlist with a specific name.
     * @param name The name of the playlist.
     */
    public Playlist(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the playlist.
     * @return The playlist name.
     */
    public String getName() {
        return name;
    }
}
