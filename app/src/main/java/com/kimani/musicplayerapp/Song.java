package com.kimani.musicplayerapp;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * The Song class represents a local audio file on the device.
 * It implements Parcelable so that song data can be passed between Activities (e.g., from MainActivity to PlayerActivity).
 */
public class Song implements Parcelable {

    private final long id;        // Unique ID from MediaStore
    private String title;         // Name of the track
    private String artist;        // Name of the artist/performer
    private final String data;    // Absolute file path on storage
    private final long albumId;   // ID of the album this song belongs to

    /**
     * Constructor to initialize a Song object.
     */
    public Song(long id, String title, String artist, String data, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.data = data;
        this.albumId = albumId;
    }

    // --- GETTER METHODS ---
    
    public long getId() { return id; }
    
    public String getTitle() { return title; }
    
    public String getArtist() { return artist; }

    /**
     * @return The absolute file path of the song. Used for media playback.
     */
    public String getPath() { return data; }

    public String getData() { return data; }
    
    public long getAlbumId() { return albumId; }

    // --- SETTER METHODS ---
    
    /**
     * Updates the song title. Useful after a successful MediaStore rename operation.
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Updates the artist name.
     */
    public void setArtist(String newArtist) {
        this.artist = newArtist;
    }

    // --- Parcelable Implementation ---

    /**
     * Constructor used by the Parcelable CREATOR to reconstruct the object.
     */
    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        data = in.readString();
        albumId = in.readLong();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Flatten the object into a parcel
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(data);
        dest.writeLong(albumId);
    }

    @Override
    public int describeContents() {
        return 0; // No special child classes
    }

    /**
     * CREATOR field required for Parcelable implementation.
     */
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
