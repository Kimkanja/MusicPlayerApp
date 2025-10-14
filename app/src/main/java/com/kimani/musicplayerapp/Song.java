package com.kimani.musicplayerapp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class song implements Parcelable {
    public long id;
    public String title;
    public String artist;
    public String date;
    public long albumId;

    public song(long id, String title, String artist, String date, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.date = date;
        this.albumId = albumId;
    }


    protected song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        date = in.readString();
        albumId = in.readLong();
    }

    public static final Creator<song> CREATOR = new Creator<song>() {
        @Override
        public song createFromParcel(Parcel in) {
            return new song(in);
        }

        @Override
        public song[] newArray(int size) {
            return new song[size];
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
