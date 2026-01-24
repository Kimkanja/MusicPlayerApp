package com.kimani.musicplayerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SongPickerActivity allows the user to browse all music files on their device 
 * and select multiple songs to add to a specific playlist.
 */
public class SongPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongPickerAdapter songPickerAdapter;
    private List<AudioModel> allSongsList;
    private String playlistName;
    
    // Tracks the songs currently selected by the user in this session
    private ArrayList<AudioModel> songsToAdd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_picker);

        // Initialize the Toolbar as the action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve the name of the playlist we are adding songs to
        playlistName = getIntent().getStringExtra("PLAYLIST_NAME");
        if (playlistName == null || playlistName.isEmpty()) {
            Toast.makeText(this, "Playlist name is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configure Action Bar title and back navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add to: " + playlistName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.songsRecyclerView);
        allSongsList = new ArrayList<>();

        setupRecyclerView();
        loadAllSongs();
    }

    /**
     * Sets up the RecyclerView with the SongPickerAdapter.
     * The adapter callback toggles song selection in the 'songsToAdd' list.
     */
    private void setupRecyclerView() {
        songPickerAdapter = new SongPickerAdapter(this, allSongsList, song -> {
            if (songsToAdd.contains(song)) {
                songsToAdd.remove(song);
            } else {
                songsToAdd.add(song);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(songPickerAdapter);
    }

    /**
     * Queries the MediaStore ContentProvider to retrieve a list of all 
     * music files available on the device's external storage.
     */
    private void loadAllSongs() {
        String[] projection = {
                MediaStore.Audio.Media.DATA, // File path
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
        };

        // Filter for music files only
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                allSongsList.add(new AudioModel(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
            cursor.close();
            songPickerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu containing the "Done" button
        getMenuInflater().inflate(R.menu.song_picker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_done) {
            // Confirm selection and save
            saveSongsToPlaylist();
            return true;
        } else if (id == android.R.id.home) {
            // Handle toolbar back button click
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves the selected songs' paths into SharedPreferences under the current playlist's key.
     * Merges new selections with existing songs already in the playlist.
     */
    private void saveSongsToPlaylist() {
        if (songsToAdd.isEmpty()) {
            Toast.makeText(this, "No songs selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Playlists", MODE_PRIVATE);
        
        // Retrieve existing songs in this playlist to avoid losing them
        Set<String> existingSongPaths = sharedPreferences.getStringSet(playlistName, new HashSet<>());
        Set<String> newSongPaths = new HashSet<>(existingSongPaths);

        // Add the paths of newly selected songs
        for (AudioModel song : songsToAdd) {
            newSongPaths.add(song.getPath());
        }

        // Commit the updated set back to SharedPreferences
        sharedPreferences.edit()
                .putStringSet(playlistName, newSongPaths)
                .apply();

        Toast.makeText(this, songsToAdd.size() + " song(s) added.", Toast.LENGTH_SHORT).show();

        // Signal success to the calling activity (PlaylistDetailsActivity)
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
