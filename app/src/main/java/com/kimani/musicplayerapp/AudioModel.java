package com.kimani.musicplayerapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * AudioModel is a data class used to represent an audio track, specifically within
 * custom playlists and the local song picker. 
 * It implements Parcelable to allow efficient passing of song data between Activities.
 */
public class AudioModel implements Parcelable {
    private String path;     // Absolute file system path to the audio file
    private String title;    // Name of the song
    private String duration; // Length of the track in milliseconds (stored as String)
    private String artist;   // Name of the artist or performer

    /**
     * Standard constructor for creating an AudioModel instance.
     *
     * @param path     The file path of the song.
     * @param title    The title of the song.
     * @param duration The duration of the song.
     * @param artist   The artist of the song.
     */
    public AudioModel(String path, String title, String duration, String artist) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.artist = artist;
    }

    // --- Parcelable Implementation ---

    /**
     * Protected constructor used by the Parcelable.Creator to rebuild the object from a Parcel.
     *
     * @param in The Parcel containing the serialized AudioModel.
     */
    protected AudioModel(Parcel in) {
        path = in.readString();
        title = in.readString();
        duration = in.readString();
        artist = in.readString();
    }

    /**
     * CREATOR field required by Parcelable to instantiate the class from a Parcel.
     */
    public static final Creator<AudioModel> CREATOR = new Creator<AudioModel>() {
        @Override
        public AudioModel createFromParcel(Parcel in) {
            return new AudioModel(in);
        }

        @Override
        public AudioModel[] newArray(int size) {
            return new AudioModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0; // No special content types used
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Flattening the object into the Parcel for transmission
        dest.writeString(path);
        dest.writeString(title);
        dest.writeString(duration);
        dest.writeString(artist);
    }

    // --- Getter Methods ---

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }
}
