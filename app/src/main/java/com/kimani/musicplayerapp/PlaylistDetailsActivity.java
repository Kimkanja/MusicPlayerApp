// C:/Users/PC/Desktop/MusicPlayerApp/kimani/MusicPlayerApp/app/src/main/java/com/kimani/musicplayerapp/PlaylistDetailsActivity.java
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

// Implements the click listener interface from MusicAdapter
public class PlaylistDetailsActivity extends AppCompatActivity implements MusicAdapter.OnSongClickListener {

    private RecyclerView playlistSongsRecyclerView;
    private FloatingActionButton addSongsFab;
    private TextView playlistNameTitle;
    private TextView emptyPlaylistView;

    private String playlistName;
    private ArrayList<AudioModel> songsInPlaylist;
    private MusicAdapter musicAdapter;

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

        playlistName = getIntent().getStringExtra("PLAYLIST_NAME");

        playlistNameTitle = findViewById(R.id.playlistNameTitle);
        playlistSongsRecyclerView = findViewById(R.id.playlistSongsRecyclerView);
        addSongsFab = findViewById(R.id.addSongsFab);
        emptyPlaylistView = findViewById(R.id.emptyPlaylistView);

        if (playlistName != null) {
            playlistNameTitle.setText(playlistName);
        }

        setupRecyclerView();
        loadSongsForPlaylist(); // Initial load

        addSongsFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SongPickerActivity.class);
            intent.putExtra("PLAYLIST_NAME", playlistName);
            songPickerLauncher.launch(intent);
        });
    }

    private void setupRecyclerView() {
        songsInPlaylist = new ArrayList<>();
        // Pass 'this' as the click listener
        musicAdapter = new MusicAdapter(this, songsInPlaylist, this);
        playlistSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistSongsRecyclerView.setAdapter(musicAdapter);
    }

    private void loadSongsForPlaylist() {
        songsInPlaylist.clear();

        SharedPreferences sharedPreferences = getSharedPreferences("Playlists", MODE_PRIVATE);
        Set<String> songPaths = sharedPreferences.getStringSet(playlistName, new HashSet<>());

        Log.d("PlaylistDetails", "Loading " + songPaths.size() + " songs for playlist: " + playlistName);

        if (!songPaths.isEmpty()) {
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

    @Override
    public void onSongClick(int position) {
        // Set the current index for highlighting in the adapter
        MyMediaPlayer.currentIndex = position;

        // Create an Intent to start PlayerActivity
        Intent intent = new Intent(this, PlayerActivity.class);

        // --- FIX: Add a specific action to the intent ---
        // This tells PlayerActivity to start a new playback session.
        intent.setAction("ACTION_START_FROM_PLAYLIST");
        // --- End of Fix ---

        // Put the list of songs and the clicked position into the intent.
        intent.putParcelableArrayListExtra("songList", songsInPlaylist);
        intent.putExtra("position", position);

        startActivity(intent);
    }




    @Override
    protected void onResume() {
        super.onResume();
        if (musicAdapter != null) {
            musicAdapter.notifyDataSetChanged();
        }
    }
}
