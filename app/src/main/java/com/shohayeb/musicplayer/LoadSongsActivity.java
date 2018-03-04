package com.shohayeb.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoadSongsActivity extends AppCompatActivity {
    public static final String SONGS_LIST = "songsList";
    public static final String INDEX = "index";
    private ListView listView;
    private SongsAdapter adapter;
    private LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_songs);
        progressLayout = findViewById(R.id.progress_bar);
        progressLayout.setVisibility(View.VISIBLE);
        ActivityCompat.requestPermissions(LoadSongsActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        listView = findViewById(R.id.list_view);
    }

    private List<Songs> findSongs(File root) {
        List<Songs> songs = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    songs.addAll(findSongs(file));
                } else {
                    if (file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") || file.getName().endsWith(".dct") || file.getName().endsWith(".gsm") ||
                            file.getName().endsWith(".wma") || file.getName().endsWith(".ogg") || file.getName().endsWith(".au") || file.getName().endsWith(".flac") ||
                            file.getName().endsWith(".m4a") || file.getName().endsWith(".msv") || file.getName().endsWith(".raw")) {
                        songs.add(getInfo(file));
                    }
                }
            }
        }
        return songs;
    }

    private Songs getInfo(File file) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getPath());
        byte[] artBytes = mmr.getEmbeddedPicture();
        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (null == title || title.equals(""))
            title = file.getName();
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        return new Songs(file.getPath(), title, duration, artist, album, artBytes);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ArrayList<Songs> songs = new ArrayList<>(findSongs(Environment.getExternalStorageDirectory()));
                    adapter = new SongsAdapter(this, songs);
                    progressLayout.setVisibility(View.GONE);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        Intent intent = new Intent(LoadSongsActivity.this, PlayerActivity.class);
                        intent.putExtra(SONGS_LIST, songs);
                        intent.putExtra(INDEX, position);
                    startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    });
                } else {
                    progressLayout.setVisibility(View.GONE);
                    Toast.makeText(LoadSongsActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}