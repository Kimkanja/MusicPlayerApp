package com.kimani.musicplayerapp.models;

/**
 * TrackInfo is a generic model used to represent song metadata in the UI.
 * It serves as a bridge between different song data sources (MediaStore and Firestore).
 * This class is also designed for easy mapping with Firebase Firestore.
 */
public class TrackInfo {
    private String id;        // Unique identifier (String to accommodate Firestore IDs)
    private String title;     // The name of the song
    private String subtitle;  // Usually stores the artist or additional info
    private String url;       // Path to the audio (local file path or remote URL)
    private String coverUrl;  // URL or path to the album art/thumbnail

    /**
     * Required empty constructor for Firebase Firestore deserialization.
     */
    public TrackInfo() {}

    /**
     * Parameterized constructor to create a TrackInfo object.
     */
    public TrackInfo(String id, String title, String subtitle, String url, String coverUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
        this.coverUrl = coverUrl;
    }

    // --- GETTER METHODS ---

    public String getId() { return id; }
    
    public String getTitle() { return title; }
    
    /**
     * @return The artist or secondary description of the track.
     */
    public String getSubtitle() { return subtitle; }
    
    /**
     * @return The data source location (URL or local path).
     */
    public String getUrl() { return url; }
    
    public String getCoverUrl() { return coverUrl; }
}
