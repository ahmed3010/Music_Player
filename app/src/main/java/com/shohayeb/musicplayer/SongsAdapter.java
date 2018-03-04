package com.shohayeb.musicplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shohayeb on 27/02/2018.
 */

public class SongsAdapter extends ArrayAdapter<Songs> {
    private Context context;

    public SongsAdapter(@NonNull Context context, @NonNull List<Songs> objects) {
        super(context, 0, objects);
        this.context = context;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        Songs currentSong = getItem(position);
        TextView titleTextView = convertView.findViewById(R.id.title_text_view);
        TextView durationTextView = convertView.findViewById(R.id.duration_text_view);
        TextView artistTextView = convertView.findViewById(R.id.artist_text_view);
        TextView albumTextView = convertView.findViewById(R.id.album_text_view);
        titleTextView.setText(currentSong.getTitle());
        durationTextView.setText(getTimeString(currentSong.getDuration()));
        artistTextView.setText(currentSong.getArtist());
        albumTextView.setText(currentSong.getAlbum());
        return convertView;
    }

    private String getTimeString(String milliSecond) {
        long millis = Long.parseLong(milliSecond);
        StringBuffer buf = new StringBuffer();
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        if (seconds == 0)
            seconds = 1;
        if (hours != 0) {
            buf.append(String.format("%02d", hours))
                    .append(":");
        }
        buf
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }
}
