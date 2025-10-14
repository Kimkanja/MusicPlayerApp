package com.kimani.musicplayerapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Song implements Parcelable {
    public long id;
    public String title;
    public String artist;
    public String date;
    public long albumId;

    public Song(long id, String title, String artist, String date, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.date = date;
        this.albumId = albumId;
    }


    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        date = in.readString();
        albumId = in.readLong();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(date);
        dest.writeLong(albumId);
    }

}
