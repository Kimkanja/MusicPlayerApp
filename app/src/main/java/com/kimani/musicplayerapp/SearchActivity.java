package com.kimani.musicplayerapp;

import android.database.Cursor;import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kimani.musicplayerapp.Adapter.SongAdapter;
import com.kimani.musicplayerapp.models.TrackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<TrackInfo> displayList = new ArrayList<>(); // List shown in UI
    private List<Song> localSongs = new ArrayList<>(); // All local songs cached
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Load Local Songs into memory for fast searching
        localSongs = getLocalSongs();

        recyclerView = findViewById(R.id.search_results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Setup Adapter
        adapter = new SongAdapter(displayList, this);
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    performSearch(newText);
                } else {
                    displayList.clear();
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    private void performSearch(String query) {
        displayList.clear();
        String lowerQuery = query.toLowerCase();

        // 3. Search Local Songs first (Instant)
        for (Song s : localSongs) {
            if (s.getTitle().toLowerCase().contains(lowerQuery) || s.getArtist().toLowerCase().contains(lowerQuery)) {
                displayList.add(new TrackInfo(
                        String.valueOf(s.getId()),
                        s.getTitle(),
                        s.getArtist(),
                        s.getPath(),
                        "" // No cover URL for local
                ));
            }
        }

        // 4. Search Firebase (Online)
        db.collection("songs")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TrackInfo track = document.toObject(TrackInfo.class);
                            // Avoid duplicates if local and online match
                            displayList.add(track);
                        }
                    }

                    if (!displayList.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Helper to fetch local files
    private List<Song> getLocalSongs() {
        List<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        try (Cursor cursor = getContentResolver().query(uri, projection, MediaStore.Audio.Media.IS_MUSIC + "!=0", null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    songs.add(new Song(
                            cursor.getLong(0), cursor.getString(1),
                            cursor.getString(2), cursor.getString(3), cursor.getLong(4)));
                }
            }
        }
        return songs;
    }

    @Override
    public void onItemClick(TrackInfo track) {
        // Find if this is a local song or online
        ArrayList<Song> playList = new ArrayList<>();
        int position = 0;

        // Check if track matches a local song
        boolean isLocal = false;
        for (int i = 0; i < localSongs.size(); i++) {
            if (String.valueOf(localSongs.get(i).getId()).equals(track.getId())) {
                isLocal = true;
                position = i;
                break;
            }
        }

        Intent intent = new Intent(this, PlayerActivity.class);

        if (isLocal) {
            // It's a local file, pass the full local list so user can skip next/prev
            intent.putParcelableArrayListExtra("songList", new ArrayList<>(localSongs));
            intent.putExtra("position", position);
        } else {
            // It's an online file, create a temporary Song object for PlayerActivity
            Song onlineSong = new Song(
                    Long.parseLong(track.getId().replaceAll("[\\D]", "0").substring(0, Math.min(track.getId().length(), 8))),
                    track.getTitle(),
                    track.getSubtitle(),
                    track.getUrl(),
                    0);

            playList.add(onlineSong);
            intent.putParcelableArrayListExtra("songList", playList);
            intent.putExtra("position", 0);
        }

        startActivity(intent);
    }
}