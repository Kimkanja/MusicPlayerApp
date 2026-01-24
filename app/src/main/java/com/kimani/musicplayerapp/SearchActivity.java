package com.kimani.musicplayerapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

/**
 * SearchActivity provides functionality to search for songs both locally on the device
 * and online via Firebase Firestore. It updates the UI in real-time as the user types.
 */
public class SearchActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener, SongAdapter.OnItemLongClickListener {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<TrackInfo> displayList = new ArrayList<>(); // List used to update the UI
    private List<Song> localSongs = new ArrayList<>();       // Cache of all local songs
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge for a modern look
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        // Adjust padding to account for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Pre-fetch local songs for faster filtering during search
        localSongs = getLocalSongs();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.search_results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapter with an empty list initially
        adapter = new SongAdapter(displayList, this, this);
        recyclerView.setAdapter(adapter);

        // Configure the SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Trigger search when the keyboard search button is pressed
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Trigger real-time search as the user types
                if (newText.length() > 0) {
                    performSearch(newText);
                } else {
                    // Clear results if the search field is empty
                    displayList.clear();
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    /**
     * Executes the search logic. Filters local songs first, then queries Firestore for online songs.
     * @param query The search string entered by the user.
     */
    private void performSearch(String query) {
        displayList.clear();
        String lowerQuery = query.toLowerCase();

        // --- Phase 1: Search Local Songs ---
        for (Song s : localSongs) {
            // Check if title or artist matches the query
            if (s.getTitle().toLowerCase().contains(lowerQuery) ||
                    s.getArtist().toLowerCase().contains(lowerQuery)) {
                displayList.add(new TrackInfo(
                        String.valueOf(s.getId()),
                        s.getTitle(),
                        s.getArtist(),
                        s.getPath(),
                        "" // Thumbnail can be added here if available
                ));
            }
        }

        // --- Phase 2: Search Online (Firebase Firestore) ---
        // Uses a range query to find titles starting with the query string
        db.collection("songs")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TrackInfo track = document.toObject(TrackInfo.class);
                            
                            // Prevent duplicates if a local song has the same ID as an online one
                            boolean isDuplicate = false;
                            for (TrackInfo existing : displayList) {
                                if (existing.getId().equals(track.getId())) {
                                    isDuplicate = true;
                                    break;
                                }
                            }
                            if (!isDuplicate) displayList.add(track);
                        }
                    }

                    // Update UI if results were found
                    if (!displayList.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Queries MediaStore for all audio files on the device.
     * @return List of Song objects.
     */
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

    /**
     * Handles clicks on search results. Converts TrackInfo to Song and launches PlayerActivity.
     */
    @Override
    public void onItemClick(TrackInfo track) {
        ArrayList<Song> singleSongList = new ArrayList<>();

        // Convert the generic TrackInfo (which could be local or online) into a Song object
        long id;
        try {
            // Attempt to parse the ID as a long (standard for local MediaStore)
            id = Long.parseLong(track.getId());
        } catch (Exception e) {
            // If it's a Firestore String ID, use its hashCode as a numeric identifier
            id = track.hashCode();
        }

        Song selectedSong = new Song(
                id,
                track.getTitle(),
                track.getSubtitle(), // Subtitle stores the artist
                track.getUrl(),      // Url stores the file path or web link
                0                   // Placeholder albumId
        );

        singleSongList.add(selectedSong);

        // Launch PlayerActivity with the single selected song
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putParcelableArrayListExtra("songList", singleSongList);
        intent.putExtra("position", 0);
        startActivity(intent);

        Toast.makeText(this, "Playing: " + track.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(TrackInfo track) {
        // Reserved for future use (e.g., show details or add to playlist)
    }
}
