// Replace this file: app/src/main/java/com/kimani/musicplayerapp/Song.java
package com.kimani.musicplayerapp;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Song implements Parcelable {

    private final long id;
    private String title;
    private String artist; // <-- This will now be modifiable
    private final String data;
    private final long albumId;

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
    public String getData() { return data; }
    public long getAlbumId() { return albumId; }

    // --- SETTER METHODS ---
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    // New setter for the artist
    public void setArtist(String newArtist) {
        this.artist = newArtist;
    }

    // --- Parcelable Implementation ---
    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        data = in.readString();
        albumId = in.readLong();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(data);
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
