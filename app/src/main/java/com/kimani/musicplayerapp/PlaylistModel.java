package com.kimani.musicplayerapp.models;

import java.util.List;

public class PlaylistModel {
    private String name;
    private List<String> songIds;

    // A no-argument constructor is required for Firestore deserialization
    public PlaylistModel() {
    }

    public PlaylistModel(String name, List<String> songIds) {
        this.name = name;
        this.songIds = songIds;
    }

    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
