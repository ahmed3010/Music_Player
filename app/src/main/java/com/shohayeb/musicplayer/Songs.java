package com.shohayeb.musicplayer;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by shohayeb on 27/02/2018.
 */

public class Songs implements Comparable, Serializable {
    private String title;
    private String duration;
    private String artist;
    private String album;
    private String path;
    private byte[] bitmap;

    public Songs(String path, String title, String duration, String artist, String album, byte[] bitmap) {
        if (album != null)
            this.album = album;
        else
            this.album = "Unknown album";
        if (artist != null)
            this.artist = artist;
        else
            this.artist = "Unknown artist";
        this.title = title;
        this.duration = duration;
        this.path = path;
        this.bitmap = bitmap;
    }

    public String getPath() {
        return path;
    }

    public byte[] getBitmap() {
        return bitmap;
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

    public String getAlbum() {
        return album;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return this.title.compareTo(((Songs) o).getTitle());
    }

}
