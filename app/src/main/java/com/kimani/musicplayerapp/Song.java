package com.kimani.musicplayerapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Song implements Parcelable {
    // Fields should be private to follow good practice (encapsulation)
    private final long id;
    public final String title;
    public final String artist;
    private final String data; // Corrected from 'date' to 'data' to hold the file path
    public final long albumId;

    // Constructor to initialize a Song object
    public Song(long id, String title, String artist, String data, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.data = data;
        this.albumId = albumId;
    }

    // --- GETTER METHODS (This is the main fix) ---
    // These methods allow other classes to safely access the private fields.
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getData() {
        return data;
    }

    public long getAlbumId() {
        return albumId;
    }


    // --- Parcelable Implementation (for passing Song objects between activities) ---

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        data = in.readString(); // Read 'data' from the parcel
        albumId = in.readLong();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(data); // Write 'data' to the parcel
        dest.writeLong(albumId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
