package com.kimani.musicplayerapp.models;

import java.io.Serializable;

/**
 * SongModel represents a song entity stored in the cloud (Firebase Firestore).
 * It implements Serializable to allow passing song objects between different components if needed.
 */
public class SongModel implements Serializable {

    private String id;        // Unique identifier for the song in the database
    private String title;     // Name of the song
    private String subtitle;  // Usually stores the Artist name
    private String url;       // The remote URL for the audio file (e.g., Firebase Storage)
    private String coverUrl;  // The remote URL for the song's cover art image

    /**
     * Default no-argument constructor.
     * Required by Firebase Firestore for data mapping to Java objects.
     */
    public SongModel() {
    }

    /**
     * Parameterized constructor to create a new SongModel instance.
     */
    public SongModel(String id, String title, String subtitle, String url, String coverUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
        this.coverUrl = coverUrl;
    }

    // --- GETTER METHODS ---

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    /**
     * @return The cloud URL where the audio file is hosted.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return The cloud URL for the song's album/cover art.
     */
    public String getCoverUrl() {
        return coverUrl;
    }
}
