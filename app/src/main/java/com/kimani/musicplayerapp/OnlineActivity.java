package com.kimani.musicplayerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.kimani.musicplayerapp.models.PlaylistModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnlineActivity extends AppCompatActivity {

    // --- Playlist Components ---
    private RecyclerView playlistRecyclerView;
    private MaterialButton createPlaylistBtn;
    private PlaylistAdapter playlistAdapter;
    private List<PlaylistModel> playlistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        // --- Initialize Playlist UI from activity_online.xml ---
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        createPlaylistBtn = findViewById(R.id.createPlaylistBtn);

        // --- Setup Playlist Functionality ---
        setupPlaylistRecyclerView();
        loadPlaylists();
        createPlaylistBtn.setOnClickListener(v -> showCreatePlaylistDialog());

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload playlists every time the screen is shown to reflect any changes
        loadPlaylists();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_online); // Set 'Online' as selected

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
                // Already here, do nothing
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

    // =====================================================================================
    //  PLAYLIST MANAGEMENT METHODS (Copied from HomeActivity)
    // =====================================================================================

    private void setupPlaylistRecyclerView() {
        playlistList = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(this, playlistList, playlistName -> {
            // Click listener for when a playlist item is tapped
            Intent intent = new Intent(OnlineActivity.this, PlaylistDetailsActivity.class);
            intent.putExtra("PLAYLIST_NAME", playlistName);
            startActivity(intent);
        });

        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playlistRecyclerView.setAdapter(playlistAdapter);
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Playlist");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("e.g., Driving Mix");
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = input.getText().toString().trim();
            if (!playlistName.isEmpty()) {
                savePlaylist(playlistName);
            } else {
                Toast.makeText(OnlineActivity.this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void savePlaylist(String name) {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        if (prefs.contains(name)) {
            Toast.makeText(this, "A playlist with this name already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(name, new HashSet<>());
        editor.apply();

        Toast.makeText(this, "Playlist '" + name + "' created", Toast.LENGTH_SHORT).show();
        loadPlaylists(); // Refresh the list
    }

    private void loadPlaylists() {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        playlistList.clear();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            List<String> songIds = new ArrayList<>();
            if (entry.getValue() instanceof Set) {
                songIds.addAll((Set<String>) entry.getValue());
            }
            playlistList.add(new PlaylistModel(entry.getKey(), songIds));
        }

        Collections.sort(playlistList, (p1, p2) -> p1.getName().compareTo(p2.getName()));

        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }
}
