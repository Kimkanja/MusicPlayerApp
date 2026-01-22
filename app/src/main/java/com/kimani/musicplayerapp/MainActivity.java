package com.kimani.musicplayerapp;

import android.Manifest;
import android.content.Intent;
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
import java.util.List;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener {

    private ActivityMainBinding binding;
    private SongAdapter adapter;
    private List<Song> songList;
    private FloatingActionButton SearchBtn;
    private List<Song> favouriteList = new ArrayList<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) loadSongs();
                else Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SearchBtn = findViewById(R.id.searchBtn);
        SearchBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            Toast.makeText(MainActivity.this, "Search Button Clicked", Toast.LENGTH_SHORT).show();
        });

        binding.recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));
        checkPermissionAndLoadSongs();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_playlist);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (itemId == R.id.bottom_playlist) {
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
            return false;
        });
    }

    private void checkPermissionAndLoadSongs() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_AUDIO :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()) {
                    songs.add(new Song(cursor.getLong(idColumn), cursor.getString(titleColumn),
                            cursor.getString(artistColumn), cursor.getString(dataColumn),
                            cursor.getLong(albumIdColumn)));
                }
            }
        }
        return songs;
    }

    private void loadSongs() {
        songList = getSongs();
        if (songList.isEmpty()) {
            Toast.makeText(this, "No Music Found!", Toast.LENGTH_LONG).show();
            binding.recyclerViewSongs.setVisibility(View.GONE);
            binding.textViewNoSongs.setVisibility(View.VISIBLE);
        } else {
            // Convert our local Song list to TrackInfo list so the SongAdapter can display them
            List<TrackInfo> trackList = new ArrayList<>();
            for (Song s : songList) {
                trackList.add(new TrackInfo(
                        String.valueOf(s.getId()),
                        s.getTitle(),
                        s.getArtist(),
                        s.getPath(),
                        "" // Local files usually don't have a web cover URL
                ));
            }

            // Updated adapter initialization
            adapter = new SongAdapter(trackList, this);
            binding.recyclerViewSongs.setAdapter(adapter);
            binding.recyclerViewSongs.setVisibility(View.VISIBLE);
            Toast.makeText(this, songList.size() + " songs loaded", Toast.LENGTH_SHORT).show();
        }
    }

    // FIX: This implementation matches the interface in SongAdapter.java
    @Override
    public void onItemClick(TrackInfo track) {
        // Find the index in our local songList that matches the ID of the clicked TrackInfo
        int position = -1;
        for (int i = 0; i < songList.size(); i++) {
            if (String.valueOf(songList.get(i).getId()).equals(track.getId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
            intent.putExtra("position", position);
            startActivity(intent);
            Toast.makeText(this, "Playing: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    // Note: If you want to use showBottomSheet, you'll need to update SongAdapter
    // to accept a second listener or use a LongClick listener.
    private void showBottomSheet(int position) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_song_options, null);

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            // Logic to delete local song
            bottomSheet.dismiss();
        });
        view.findViewById(R.id.btnRename).setOnClickListener(v -> {
            showRenameDialog(position);
            bottomSheet.dismiss();
        });
        view.findViewById(R.id.btnFavorite).setOnClickListener(v -> {
            addToFavourites(songList.get(position));
            bottomSheet.dismiss();
        });

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }

    private void showRenameDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Song Name & Artist Info");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rename_song, null);
        EditText inputTitle = view.findViewById(R.id.input_song_title);
        EditText inputArtist = view.findViewById(R.id.input_song_artist);
        builder.setView(view);

        Song currentSong = songList.get(position);
        inputTitle.setText(currentSong.getTitle());
        inputArtist.setText(currentSong.getArtist());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            String newArtist = inputArtist.getText().toString().trim();

            if (!newTitle.isEmpty() && !newArtist.isEmpty()) {
                songList.get(position).setTitle(newTitle);
                songList.get(position).setArtist(newArtist);
                loadSongs(); // Refresh list
                Toast.makeText(this, "Song updated!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addToFavourites(Song song) {
        favouriteList.add(song);
        Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show();
    }
}