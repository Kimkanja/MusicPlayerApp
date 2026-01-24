package com.kimani.musicplayerapp.models;

import java.util.List;

/**
 * Model class representing a playlist stored in Firestore.
 * Contains the playlist name and a list of song IDs (usually file paths or unique identifiers) associated with it.
 */
public class PlaylistModel {
    private String name;
    private List<String> songIds;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public PlaylistModel() {
    }

    /**
     * Constructs a PlaylistModel with a name and a list of song identifiers.
     * @param name The name of the playlist.
     * @param songIds A list of strings identifying the songs in this playlist.
     */
    public PlaylistModel(String name, List<String> songIds) {
        this.name = name;
        this.songIds = songIds;
    }

    // --- Getters and Setters ---

    /**
     * @return The name of the playlist.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set for the playlist.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The list of song identifiers in this playlist.
     */
    public List<String> getSongIds() {
        return songIds;
    }

    /**
     * @param songIds The list of song identifiers to set for this playlist.
     */
    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
