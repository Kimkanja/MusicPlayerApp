package com.kimani.musicplayerapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity that displays the songs within a specific playlist.
 * It allows users to view songs, add new songs via a picker, and start playback.
 */
public class PlaylistDetailsActivity extends AppCompatActivity implements MusicAdapter.OnSongClickListener {

    private RecyclerView playlistSongsRecyclerView;
    private FloatingActionButton addSongsFab;
    private TextView playlistNameTitle;
    private TextView emptyPlaylistView;

    private String playlistName;
    private ArrayList<AudioModel> songsInPlaylist;
    private MusicAdapter musicAdapter;

    /**
     * Launcher for SongPickerActivity. Reloads the song list if songs were added.
     */
    private final ActivityResultLauncher<Intent> songPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d("PlaylistDetails", "Returned from SongPicker. Reloading songs.");
                    Toast.makeText(this, "Updating playlist...", Toast.LENGTH_SHORT).show();
                    loadSongsForPlaylist();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_details);

        // Get the playlist name passed from the previous activity
        playlistName = getIntent().getStringExtra("PLAYLIST_NAME");

        playlistNameTitle = findViewById(R.id.playlistNameTitle);
        playlistSongsRecyclerView = findViewById(R.id.playlistSongsRecyclerView);
        addSongsFab = findViewById(R.id.addSongsFab);
        emptyPlaylistView = findViewById(R.id.emptyPlaylistView);

        if (playlistName != null) {
            playlistNameTitle.setText(playlistName);
        }

        setupRecyclerView();
        loadSongsForPlaylist(); // Initial load of songs from SharedPreferences

        // FAB to open the song picker activity
        addSongsFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SongPickerActivity.class);
            intent.putExtra("PLAYLIST_NAME", playlistName);
            songPickerLauncher.launch(intent);
        });
    }

    /**
     * Initializes the RecyclerView and its adapter.
     */
    private void setupRecyclerView() {
        songsInPlaylist = new ArrayList<>();
        // Pass 'this' as the click listener to handle song selection
        musicAdapter = new MusicAdapter(this, songsInPlaylist, this);
        playlistSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistSongsRecyclerView.setAdapter(musicAdapter);
    }

    /**
     * Loads songs associated with this playlist from SharedPreferences and fetches their metadata from MediaStore.
     */
    private void loadSongsForPlaylist() {
        songsInPlaylist.clear();

        // Retrieve song paths stored for this playlist
        SharedPreferences sharedPreferences = getSharedPreferences("Playlists", MODE_PRIVATE);
        Set<String> songPaths = sharedPreferences.getStringSet(playlistName, new HashSet<>());

        Log.d("PlaylistDetails", "Loading " + songPaths.size() + " songs for playlist: " + playlistName);

        if (!songPaths.isEmpty()) {
            // Build the SQL selection string and arguments for MediaStore query
            String[] selectionArgs = songPaths.toArray(new String[0]);
            StringBuilder selection = new StringBuilder();
            for (int i = 0; i < songPaths.size(); i++) {
                selection.append(MediaStore.Audio.Media.DATA).append("=?");
                if (i < songPaths.size() - 1) {
                    selection.append(" OR ");
                }
            }

            String[] projection = {
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ARTIST,
            };

            // Query MediaStore for metadata of the files in the playlist
            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection.toString(),
                    selectionArgs,
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    songsInPlaylist.add(new AudioModel(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3)
                    ));
                }
                cursor.close();
            }
        }

        // Toggle visibility between RecyclerView and the empty state view
        if (songsInPlaylist.isEmpty()) {
            playlistSongsRecyclerView.setVisibility(View.GONE);
            emptyPlaylistView.setVisibility(View.VISIBLE);
            emptyPlaylistView.setText("Playlist is empty. Tap the '+' button to add songs!");
        } else {
            playlistSongsRecyclerView.setVisibility(View.VISIBLE);
            emptyPlaylistView.setVisibility(View.GONE);
        }

        musicAdapter.notifyDataSetChanged();
        Log.d("PlaylistDetails", "Adapter notified. Total songs in list: " + songsInPlaylist.size());
    }

    /**
     * Handles clicks on individual songs in the list.
     * Launches PlayerActivity to start playback of the selected song and the rest of the playlist.
     */
    @Override
    public void onSongClick(int position) {
        // Track current index globally (if needed by other parts of the app)
        MyMediaPlayer.currentIndex = position;

        Intent intent = new Intent(this, PlayerActivity.class);

        // Signal to PlayerActivity that playback is starting from a playlist context
        intent.setAction("ACTION_START_FROM_PLAYLIST");

        // Pass the playlist and the selected starting position
        intent.putParcelableArrayListExtra("songList", songsInPlaylist);
        intent.putExtra("position", position);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure UI stays in sync if returning from another activity
        if (musicAdapter != null) {
            musicAdapter.notifyDataSetChanged();
        }
    }
}
