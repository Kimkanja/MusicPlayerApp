// app/src/main/java/com/kimani/musicplayerapp/HomeActivity.java
package com.kimani.musicplayerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kimani.musicplayerapp.Adapter.CategoryAdapter;
import com.kimani.musicplayerapp.Adapter.SectionSongListAdapter;
import com.kimani.musicplayerapp.databinding.ActivityHomeBinding;
import com.kimani.musicplayerapp.models.CategoryModel;
import com.kimani.musicplayerapp.models.PlaylistModel;
import com.kimani.musicplayerapp.models.SongModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HomeActivity serves as the main dashboard for the online music experience.
 * It displays song categories, featured sections, and allows for local playlist management.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private CategoryAdapter categoryAdapter;

    // --- Playlist Components ---
    private RecyclerView playlistRecyclerView;
    private MaterialButton createPlaylistBtn;
    private PlaylistAdapter playlistAdapter;
    private List<PlaylistModel> playlistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enables edge-to-edge display for a modern look
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Initialize UI components ---
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        createPlaylistBtn = findViewById(R.id.createPlaylistBtn);

        // Check for network availability before loading data from Firebase
        if (NetworkUtils.isNetworkAvailable(this)) {
            loadDataFromFirebase();
        } else {
            showNoInternetDialog();
        }

        setupBottomNavigation();
        setupLogoutButton();

        // --- Initialize Playlist System ---
        if (playlistRecyclerView != null && createPlaylistBtn != null) {
            setupPlaylistRecyclerView();
            loadPlaylists();
            createPlaylistBtn.setOnClickListener(v -> showCreatePlaylistDialog());
        }
    }

    /**
     * Fetches categories and sections from Firestore and populates the UI.
     */
    private void loadDataFromFirebase() {
        setLoading(true);
        getCategories();
        // Setup different sections on the home screen
        setupSection("section_1", binding.section1MainLayout, binding.section1Title, binding.section1RecyclerView);
        setupSection("section_2", binding.section2MainLayout, binding.section2Title, binding.section2RecyclerView);
        setupSection("section_3", binding.section3MainLayout, binding.section3Title, binding.section3RecyclerView);
        setupMostlyPlayed("mostly_played", binding.mostlyPlayedMainLayout, binding.mostlyPlayedTitle, binding.mostlyPlayedRecyclerView);
        
        // Hide loading indicator after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> setLoading(false), 1500);
    }

    /**
     * Toggles the visibility of the loading indicator and main content.
     */
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.loadingIndicator.setVisibility(View.VISIBLE);
            binding.contentLayout.setVisibility(View.GONE);
        } else {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.contentLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows a dialog when there is no internet connection, offering retry or offline mode.
     */
    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need an internet connection to access online content. Please connect and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (NetworkUtils.isNetworkAvailable(HomeActivity.this)) {
                        loadDataFromFirebase();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("Offline Mode", (dialog, which) -> {
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Configures the BottomNavigationView and its navigation logic.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                // Refresh home data if already here
                if (NetworkUtils.isNetworkAvailable(this)) {
                    loadDataFromFirebase();
                } else {
                    showNoInternetDialog();
                }
                return true;
            } else if (itemId == R.id.bottom_playlist) {
                // Stop online playback before switching to local player
                if (MyExoplayer.getInstance() != null) {
                    MyExoplayer.getInstance().stop();
                }
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
            return false;
        });
    }

    /**
     * Configures the logout button to sign out from Firebase and return to LoginActivity.
     */
    private void setupLogoutButton() {
        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the mini-player view and refresh playlists
        showPlayerView();
        if (playlistRecyclerView != null) {
            loadPlaylists();
        }
    }

    /**
     * Updates the mini-player UI if a song is currently playing.
     */
    public void showPlayerView() {
        binding.playerView.setOnClickListener(v -> {
            startActivity(new Intent(this, OnlinePlayerActivity.class));
            overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
        });

        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null && MyExoplayer.getInstance() != null && MyExoplayer.getInstance().isPlaying()) {
            binding.playerView.setVisibility(View.VISIBLE);
            binding.songTitleTextView.setText("Now Playing : " + currentSong.getTitle());
            Glide.with(binding.songCoverImageView)
                    .load(currentSong.getCoverUrl())
                    .apply(new RequestOptions().transform(new RoundedCorners(32)))
                    .into(binding.songCoverImageView);
            Glide.with(binding.songGifImageView)
                    .load(R.drawable.media_playing)
                    .circleCrop()
                    .into(binding.songGifImageView);
        } else {
            binding.playerView.setVisibility(View.GONE);
        }
    }

    /**
     * Fetches music categories from Firestore.
     */
    void getCategories() {
        FirebaseFirestore.getInstance().collection("category")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CategoryModel> categoryList = queryDocumentSnapshots.toObjects(CategoryModel.class);
                    setupCategoryRecyclerView(categoryList);
                });
    }

    /**
     * Sets up the RecyclerView for categories.
     */
    void setupCategoryRecyclerView(List<CategoryModel> categoryList) {
        categoryAdapter = new CategoryAdapter(categoryList);
        binding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecyclerView.setAdapter(categoryAdapter);
    }

    /**
     * Sets up a horizontal song section (like 'Popular' or 'Recommended').
     */
    public void setupSection(String id, RelativeLayout mainLayout, TextView titleView, RecyclerView recyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    CategoryModel section = documentSnapshot.toObject(CategoryModel.class);
                    if (section != null) {
                        mainLayout.setVisibility(View.VISIBLE);
                        titleView.setText(section.getName());
                        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        recyclerView.setAdapter(new SectionSongListAdapter(section.getSongs()));
                        mainLayout.setOnClickListener(v -> {
                            SongsListActivity.category = section;
                            startActivity(new Intent(HomeActivity.this, SongsListActivity.class));
                        });
                    }
                });
    }

    /**
     * Sets up the 'Mostly Played' section by querying Firestore for top-played songs.
     */
    public void setupMostlyPlayed(String id, RelativeLayout mainLayout, TextView titleView, RecyclerView recyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
                .document(id)
                .get()
                .addOnSuccessListener(sectionDocumentSnapshot ->
                        FirebaseFirestore.getInstance().collection("songs")
                                .orderBy("count", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(5)
                                .get()
                                .addOnSuccessListener(songListSnapshot -> {
                                    List<SongModel> songsModelList = songListSnapshot.toObjects(SongModel.class);
                                    List<String> songsIdList = songsModelList.stream()
                                            .map(SongModel::getId)
                                            .collect(Collectors.toList());

                                    CategoryModel section = sectionDocumentSnapshot.toObject(CategoryModel.class);
                                    if (section != null) {
                                        section.setSongs(songsIdList);
                                        mainLayout.setVisibility(View.VISIBLE);
                                        titleView.setText(section.getName());
                                        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                        recyclerView.setAdapter(new SectionSongListAdapter(section.getSongs()));
                                        mainLayout.setOnClickListener(v -> {
                                            SongsListActivity.category = section;
                                            startActivity(new Intent(HomeActivity.this, SongsListActivity.class));
                                        });
                                    }
                                })
                );
    }

    // =====================================================================================
    // LOCAL PLAYLIST MANAGEMENT METHODS
    // =====================================================================================

    /**
     * Initializes the RecyclerView for displaying local playlists.
     */
    private void setupPlaylistRecyclerView() {
        playlistList = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(this, playlistList, (playlistName) -> {
            // Open PlaylistDetailsActivity when a playlist is clicked
            Intent intent = new Intent(HomeActivity.this, PlaylistDetailsActivity.class);
            intent.putExtra("PLAYLIST_NAME", playlistName);
            startActivity(intent);
        });

        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playlistRecyclerView.setAdapter(playlistAdapter);
    }

    /**
     * Shows a dialog to input a name for a new local playlist.
     */
    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Playlist");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("e.g., Chill Vibes");
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = input.getText().toString().trim();
            if (!playlistName.isEmpty()) {
                savePlaylist(playlistName);
            } else {
                Toast.makeText(HomeActivity.this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Persists a new playlist name into SharedPreferences.
     */
    private void savePlaylist(String name) {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        if (prefs.contains(name)) {
            Toast.makeText(this, "Playlist with this name already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        // Initialize with an empty set of song paths/IDs
        editor.putStringSet(name, new HashSet<>());
        editor.apply();

        Toast.makeText(this, "Playlist '" + name + "' created", Toast.LENGTH_SHORT).show();
        loadPlaylists(); // Refresh list
    }

    /**
     * Loads all playlists stored in SharedPreferences and updates the adapter.
     */
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

        // Sort playlists alphabetically
        Collections.sort(playlistList, (p1, p2) -> p1.getName().compareTo(p2.getName()));

        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }
}


/**
 * Adapter for displaying local playlists in a RecyclerView.
 */
class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final Context context;
    private final List<PlaylistModel> playlistList;
    private final OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(String playlistName);
    }

    public PlaylistAdapter(Context context, List<PlaylistModel> playlistList, OnPlaylistClickListener listener) {
        this.context = context;
        this.playlistList = playlistList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistModel playlist = playlistList.get(position);
        holder.playlistName.setText(playlist.getName());

        // Set the static icon for playlists
        holder.playlistIcon.setImageResource(R.drawable.icon_playlist);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistClick(playlist.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView playlistIcon;
        TextView playlistName;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistIcon = itemView.findViewById(R.id.playlistIcon);
            playlistName = itemView.findViewById(R.id.playlistName);
        }
    }
}
