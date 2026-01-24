package com.kimani.musicplayerapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kimani.musicplayerapp.Adapter.SongAdapter;
import com.kimani.musicplayerapp.databinding.ActivityMainBinding;
import com.kimani.musicplayerapp.models.TrackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MainActivity serves as the primary screen for displaying local music files.
 * It handles permission requests, fetches audio files from the device storage,
 * and provides options to play, rename, delete, or favorite songs.
 */
public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener, SongAdapter.OnItemLongClickListener {

    // Request codes for Scoped Storage operations (Android 10+)
    private static final int REQUEST_RENAME_SONG = 101;
    private static final int REQUEST_DELETE_SONG = 102;

    // Temporary storage for song metadata during rename/delete operations
    private Song pendingSong;
    private String pendingTitle;
    private String pendingArtist;

    // UI elements and adapters
    private ActivityMainBinding binding;
    private SongAdapter adapter;
    private List<Song> songList;
    private FloatingActionButton SearchBtn;

    // Permission launcher to handle the result of the runtime permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // If permission is granted, load the music files
                    loadSongs();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display for modern UI
        EdgeToEdge.enable(this);
        // Use View Binding for cleaner UI code
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Search Button and set its click listener
        SearchBtn = findViewById(R.id.searchBtn);
        SearchBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));

        // Setup RecyclerView with a LinearLayoutManager
        binding.recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));
        
        // Check for necessary permissions before loading songs
        checkPermissionAndLoadSongs();

        // Setup the bottom navigation bar
        setupBottomNavigation();
    }

    /**
     * Configures the BottomNavigationView to handle navigation between different activities.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_playlist);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (itemId == R.id.bottom_online) {
                startActivity(new Intent(getApplicationContext(), OnlineActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return itemId == R.id.bottom_playlist;
        });
    }

    /**
     * Checks if the app has the required storage permissions and requests them if not.
     */
    private void checkPermissionAndLoadSongs() {
        // Use different permissions based on Android version
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * Queries the device's MediaStore for audio files.
     * @return A list of Song objects containing music metadata.
     */
    private List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        // Querying for music files that are not 0 in the MediaStore
        try (Cursor cursor = getContentResolver().query(uri, projection, MediaStore.Audio.Media.IS_MUSIC + "!=0", null, MediaStore.Audio.Media.TITLE + " ASC")) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()) {
                    songs.add(new Song(cursor.getLong(idCol), cursor.getString(titleCol),
                            cursor.getString(artistCol), cursor.getString(dataCol), cursor.getLong(albumCol)));
                }
            }
        }
        return songs;
    }

    /**
     * Loads songs into the RecyclerView and handles the empty state UI.
     */
    private void loadSongs() {
        songList = getSongs();
        if (songList.isEmpty()) {
            binding.recyclerViewSongs.setVisibility(View.GONE);
            binding.textViewNoSongs.setVisibility(View.VISIBLE);
        } else {
            // Convert Song objects to TrackInfo for the Adapter
            List<TrackInfo> trackList = new ArrayList<>();
            for (Song s : songList) {
                trackList.add(new TrackInfo(String.valueOf(s.getId()), s.getTitle(), s.getArtist(), s.getPath(), ""));
            }
            adapter = new SongAdapter(trackList, this, this);
            binding.recyclerViewSongs.setAdapter(adapter);
            binding.recyclerViewSongs.setVisibility(View.VISIBLE);
            binding.textViewNoSongs.setVisibility(View.GONE);
        }
    }

    /**
     * Implementation of OnItemClickListener. Starts PlayerActivity with the selected song.
     */
    @Override
    public void onItemClick(TrackInfo track) {
        int position = -1;
        for (int i = 0; i < songList.size(); i++) {
            if (String.valueOf(songList.get(i).getId()).equals(track.getId())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            Intent intent = new Intent(this, PlayerActivity.class);
            // Pass the entire song list and the current position to the player
            intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    /**
     * Implementation of OnItemLongClickListener. Shows options for the selected song.
     */
    @Override
    public void onItemLongClick(TrackInfo track) {
        for (int i = 0; i < songList.size(); i++) {
            if (String.valueOf(songList.get(i).getId()).equals(track.getId())) {
                showBottomSheet(i);
                break;
            }
        }
    }

    /**
     * Displays a BottomSheetDialog with options to Delete, Rename, or Favorite a song.
     */
    private void showBottomSheet(int position) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_song_options, null);
        Song song = songList.get(position);

        // Delete Button Logic
        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Request user permission for deletion on Android 11+
                PendingIntent pi = MediaStore.createDeleteRequest(getContentResolver(), Collections.singletonList(songUri));
                try {
                    startIntentSenderForResult(pi.getIntentSender(), REQUEST_DELETE_SONG, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) { e.printStackTrace(); }
            } else {
                // Direct deletion for older Android versions
                getContentResolver().delete(songUri, null, null);
                loadSongs();
            }
            bottomSheet.dismiss();
        });

        // Rename Button Logic
        view.findViewById(R.id.btnRename).setOnClickListener(v -> {
            showRenameDialog(position);
            bottomSheet.dismiss();
        });

        // Favorite Button Logic
        view.findViewById(R.id.btnFavorite).setOnClickListener(v -> {
            addToFavourites(song);
            bottomSheet.dismiss();
        });

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }

    /**
     * Displays a dialog to allow the user to edit song Title and Artist.
     */
    private void showRenameDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Song");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rename_song, null);
        EditText inputTitle = view.findViewById(R.id.input_song_title);
        EditText inputArtist = view.findViewById(R.id.input_song_artist);
        builder.setView(view);

        Song currentSong = songList.get(position);
        inputTitle.setText(currentSong.getTitle());
        inputArtist.setText(currentSong.getArtist());

        builder.setPositiveButton("Save", (dialog, which) -> {
            pendingTitle = inputTitle.getText().toString().trim();
            pendingArtist = inputArtist.getText().toString().trim();
            pendingSong = currentSong;
            if (pendingTitle.isEmpty()) return;

            Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong.getId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Request write permission for Android 11+ before updating metadata
                PendingIntent pi = MediaStore.createWriteRequest(getContentResolver(), Collections.singletonList(songUri));
                try {
                    startIntentSenderForResult(pi.getIntentSender(), REQUEST_RENAME_SONG, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) { e.printStackTrace(); }
            } else {
                updateSongMetadata(songUri, pendingTitle, pendingArtist);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Updates the song's metadata in the MediaStore.
     */
    private void updateSongMetadata(Uri uri, String title, String artist) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.TITLE, title);
        values.put(MediaStore.Audio.Media.ARTIST, artist);
        if (getContentResolver().update(uri, values, null, null) > 0) {
            loadSongs(); // Refresh the list
            Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the results of PendingIntent requests (Delete/Rename permission).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_RENAME_SONG && pendingSong != null) {
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, pendingSong.getId());
                updateSongMetadata(uri, pendingTitle, pendingArtist);
            } else if (requestCode == REQUEST_DELETE_SONG) {
                loadSongs();
            }
        }
    }

    /**
     * Adds a song's ID to the Favorites set in SharedPreferences.
     */
    private void addToFavourites(Song song) {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        // Important: Create a new HashSet from the existing one to ensure changes are detected
        Set<String> favorites = new HashSet<>(prefs.getStringSet("Favorites", new HashSet<>()));
        String songId = String.valueOf(song.getId());

        if (!favorites.contains(songId)) {
            favorites.add(songId);
            prefs.edit().putStringSet("Favorites", favorites).apply();
            Toast.makeText(this, "Added to Favorites Playlist", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Already in Favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
