package com.kimani.musicplayerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.kimani.musicplayerapp.databinding.DialogCreatePlaylistBinding;
import com.kimani.musicplayerapp.models.PlaylistModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OnlineActivity handles the display and management of user-created playlists.
 * It allows users to view existing playlists, create new ones, and navigate to playlist details.
 * Playlists are persisted using SharedPreferences.
 */
public class OnlineActivity extends AppCompatActivity {

    private RecyclerView playlistRecyclerView;
    private MaterialButton createPlaylistBtn;
    private PlaylistAdapter playlistAdapter;
    private List<PlaylistModel> playlistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        // Initialize UI components
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        createPlaylistBtn = findViewById(R.id.createPlaylistBtn);

        // Setup the Horizontal RecyclerView for playlists
        setupPlaylistRecyclerView();
        
        // Initial load of playlists from storage
        loadPlaylists();

        // Button listener to trigger the 'Create Playlist' dialog
        createPlaylistBtn.setOnClickListener(v -> showCreatePlaylistDialog());
        
        // Setup bottom navigation menu
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the playlist list whenever the activity becomes active again
        loadPlaylists();
    }

    /**
     * Configures the BottomNavigationView and its navigation logic.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_online);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (itemId == R.id.bottom_playlist) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (itemId == R.id.bottom_online) {
                // Already in OnlineActivity
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

    /**
     * Initializes the RecyclerView with a horizontal layout and the PlaylistAdapter.
     */
    private void setupPlaylistRecyclerView() {
        playlistList = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(this, playlistList, playlistName -> {
            // When a playlist is clicked, navigate to its details screen
            Intent intent = new Intent(OnlineActivity.this, PlaylistDetailsActivity.class);
            intent.putExtra("PLAYLIST_NAME", playlistName);
            startActivity(intent);
        });

        // Horizontal scrolling for a modern feel
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playlistRecyclerView.setAdapter(playlistAdapter);
    }

    /**
     * Shows a Material Alert Dialog to input a new playlist name.
     */
    private void showCreatePlaylistDialog() {
        // Use View Binding for the custom dialog layout
        DialogCreatePlaylistBinding binding = DialogCreatePlaylistBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(binding.getRoot())
                .setPositiveButton("Create", (d, which) -> {
                    String playlistName = binding.inputPlaylistName.getText().toString().trim();
                    if (!playlistName.isEmpty()) {
                        savePlaylist(playlistName);
                    } else {
                        Toast.makeText(OnlineActivity.this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        // Apply a custom background to the dialog window for the 'glow' effect
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.glow_effect_item);
        }
        dialog.show();
    }

    /**
     * Saves a new playlist name into SharedPreferences as a key with an empty HashSet.
     * @param name The name of the new playlist.
     */
    private void savePlaylist(String name) {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        if (prefs.contains(name)) {
            Toast.makeText(this, "A playlist with this name already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        // Playlists are stored as StringSets containing song IDs
        prefs.edit().putStringSet(name, new HashSet<>()).apply();
        loadPlaylists(); // Refresh the UI
    }

    /**
     * Loads all playlists from SharedPreferences and updates the adapter.
     */
    private void loadPlaylists() {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        playlistList.clear();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            // We only care about entries that are Sets (our playlists)
            if (entry.getValue() instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> idSet = (Set<String>) entry.getValue();
                List<String> songIds = new ArrayList<>(idSet);
                playlistList.add(new PlaylistModel(entry.getKey(), songIds));
            }
        }

        // Sort playlists alphabetically by name
        Collections.sort(playlistList, (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        
        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }
}
