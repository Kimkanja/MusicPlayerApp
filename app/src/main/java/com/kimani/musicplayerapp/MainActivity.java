package com.kimani.musicplayerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kimani.musicplayerapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener {
    private ActivityMainBinding binding;
    private SongAdapter adapter;
    private List<Song> songList;

    // ActivityResultLauncher for requesting permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted, load the songs
                    loadSongs();
                } else {
                    // Permission is denied
                    Toast.makeText(this, "Permission Denied! Cannot load songs.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- FIX for the crash ---
        // A RecyclerView must have a LayoutManager to function.
        binding.recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));

        // Start the process to check permissions and load songs
        checkPermissionAndLoadSongs();
    }

    /**
     * Checks for the appropriate media permission based on the Android version.
     * If permission is granted, it loads the songs.
     * If not, it launches the permission request.
     */
    private void checkPermissionAndLoadSongs() {
        String permission;
        // Check Android version to request the correct permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            // Below Android 13
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            loadSongs();
        } else {
            // Permission is not granted, request it
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * Queries the device's MediaStore to get a list of all audio files.
     * @return A List of Song objects.
     */
    private List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        // Define the columns you want to retrieve
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        // Use a try-with-resources block to auto-close the cursor
        try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, sortOrder)) {
            if (cursor != null) {
                // Get column indices
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                // Iterate over the cursor
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String data = cursor.getString(dataColumn);
                    long albumId = cursor.getLong(albumIdColumn);

                    // Add the new Song object to the list
                    songs.add(new Song(id, title, artist, data, albumId));
                }
            }
        } catch (Exception e) {
            // Log any exceptions
            e.printStackTrace();
        }
        return songs;
    }

    /**
     * Loads the songs into the RecyclerView and displays a toast with the count.
     */
    private void loadSongs() {
        songList = getSongs();

        // Check if the song list is empty
        if (songList.isEmpty()) {
            Toast.makeText(this, "Sorry, No Music found on this Device!", Toast.LENGTH_LONG).show();
            // Hide the RecyclerView if no songs are found
            binding.recyclerViewSongs.setVisibility(View.GONE);
            // Show the textViewNoSongs if no songs are found
            binding.textViewNoSongs.setVisibility(View.VISIBLE);
        } else {
            // Use a plurals resource for a grammatically correct toast message
            String message = getResources().getQuantityString(R.plurals.numberOfSongsFound, songList.size(), songList.size());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Set up the adapter with the list of songs
            adapter = new SongAdapter(songList, this);
            binding.recyclerViewSongs.setAdapter(adapter);
            // Ensure the RecyclerView is visible
            binding.recyclerViewSongs.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles clicks on items in the RecyclerView.
     * This method is required by the SongAdapter.OnItemClickListener interface.
     * @param position The position of the clicked item.
     */
    @Override
    public void OnItemClick(int position) {
        // TODO: Implement what happens when a song is clicked.
        // For example, start the PlayerActivity.
        //
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
        intent.putExtra("position", position);
        startActivity(intent);

        Toast.makeText(this, "Clicked on: " + songList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
    }
}
