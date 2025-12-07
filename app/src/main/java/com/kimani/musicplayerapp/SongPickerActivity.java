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
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongPickerAdapter songPickerAdapter;
    private List<AudioModel> allSongsList;
    private String playlistName;
    private ArrayList<AudioModel> songsToAdd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_picker);

        // --- NEW: Set up the Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // --- End of new code ---

        playlistName = getIntent().getStringExtra("PLAYLIST_NAME");
        if (playlistName == null || playlistName.isEmpty()) {
            Toast.makeText(this, "Playlist name is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use the action bar to set the title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add to: " + playlistName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Optional: adds a back arrow
        }

        recyclerView = findViewById(R.id.songsRecyclerView);
        allSongsList = new ArrayList<>();

        setupRecyclerView();
        loadAllSongs();
    }

    // ... (The rest of your loadAllSongs() and setupRecyclerView() methods remain the same) ...

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

    private void loadAllSongs() {
        String[] projection = {
                MediaStore.Audio.Media.DATA, // Path
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
        };

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
        // This method inflates your menu resource file and adds the "Done" item to the action bar.
        getMenuInflater().inflate(R.menu.song_picker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // This handles clicks on the action bar items.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            saveSongsToPlaylist(); // This is your confirmation logic.
            return true;
        } else if (id == android.R.id.home) {
            // Handle click on the back arrow
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveSongsToPlaylist() {
        if (songsToAdd.isEmpty()) {
            Toast.makeText(this, "No songs selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Playlists", MODE_PRIVATE);
        Set<String> existingSongPaths = sharedPreferences.getStringSet(playlistName, new HashSet<>());

        Set<String> newSongPaths = new HashSet<>(existingSongPaths);
        for (AudioModel song : songsToAdd) {
            newSongPaths.add(song.getPath());
        }

        sharedPreferences.edit()
                .putStringSet(playlistName, newSongPaths)
                .apply();

        Toast.makeText(this, songsToAdd.size() + " song(s) added.", Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
