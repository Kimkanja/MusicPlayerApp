// app/src/main/java/com/kimani/musicplayerapp/HomeActivity.java
package com.kimani.musicplayerapp;

import android.content.Context;
import android.content.Intent;import android.content.SharedPreferences;
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
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Initialize Playlist UI ---
        // Since the XML for playlists is in a different layout, we find them by ID
        // Make sure these IDs exist in your activity_home.xml
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
        createPlaylistBtn = findViewById(R.id.createPlaylistBtn);

        if (NetworkUtils.isNetworkAvailable(this)) {
            loadDataFromFirebase();
        } else {
            showNoInternetDialog();
        }

        setupBottomNavigation();
        setupLogoutButton();

        // --- Setup Playlist Functionality ---
        if (playlistRecyclerView != null && createPlaylistBtn != null) {
            setupPlaylistRecyclerView();
            loadPlaylists();
            createPlaylistBtn.setOnClickListener(v -> showCreatePlaylistDialog());
        }
    }

    private void loadDataFromFirebase() {
        setLoading(true);
        getCategories();
        setupSection("section_1", binding.section1MainLayout, binding.section1Title, binding.section1RecyclerView);
        setupSection("section_2", binding.section2MainLayout, binding.section2Title, binding.section2RecyclerView);
        setupSection("section_3", binding.section3MainLayout, binding.section3Title, binding.section3RecyclerView);
        setupMostlyPlayed("mostly_played", binding.mostlyPlayedMainLayout, binding.mostlyPlayedTitle, binding.mostlyPlayedRecyclerView);
        new Handler(Looper.getMainLooper()).postDelayed(() -> setLoading(false), 1500);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.loadingIndicator.setVisibility(View.VISIBLE);
            binding.contentLayout.setVisibility(View.GONE);
        } else {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.contentLayout.setVisibility(View.VISIBLE);
        }
    }

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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                if (NetworkUtils.isNetworkAvailable(this)) {
                    loadDataFromFirebase();
                } else {
                    showNoInternetDialog();
                }
                return true;
            } else if (itemId == R.id.bottom_playlist) {
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
        showPlayerView();
        // Also reload playlists in onResume to reflect any changes made elsewhere
        if (playlistRecyclerView != null) {
            loadPlaylists();
        }
    }

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

    void getCategories() {
        FirebaseFirestore.getInstance().collection("category")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CategoryModel> categoryList = queryDocumentSnapshots.toObjects(CategoryModel.class);
                    setupCategoryRecyclerView(categoryList);
                });
    }

    void setupCategoryRecyclerView(List<CategoryModel> categoryList) {
        categoryAdapter = new CategoryAdapter(categoryList);
        binding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecyclerView.setAdapter(categoryAdapter);
    }

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
    // NEW METHODS FOR LOCAL PLAYLIST MANAGEMENT
    // =====================================================================================

    private void setupPlaylistRecyclerView() {
        playlistList = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(this, playlistList, (playlistName) -> {
            // Click listener for when a playlist item is tapped
            Intent intent = new Intent(HomeActivity.this, PlaylistDetailsActivity.class);
            intent.putExtra("PLAYLIST_NAME", playlistName);
            startActivity(intent);
        });

        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playlistRecyclerView.setAdapter(playlistAdapter);
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Playlist");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("e.g., Chill Vibes");
        input.setPadding(50, 40, 50, 40); // Add some padding
        builder.setView(input);

        // Set up the buttons
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

    private void savePlaylist(String name) {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        // Check if playlist already exists
        if (prefs.contains(name)) {
            Toast.makeText(this, "Playlist with this name already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        // We store an empty set initially. Songs will be added later.
        editor.putStringSet(name, new HashSet<>());
        editor.apply();

        Toast.makeText(this, "Playlist '" + name + "' created", Toast.LENGTH_SHORT).show();
        loadPlaylists(); // Refresh the list to show the new playlist
    }

    private void loadPlaylists() {
        SharedPreferences prefs = getSharedPreferences("Playlists", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        playlistList.clear();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            // *** FIX: Use the correct constructor by providing the name and a list of song IDs ***
            List<String> songIds = new ArrayList<>();
            // The value from SharedPreferences for a stringSet is a Set. We must cast it.
            if (entry.getValue() instanceof Set) {
                // We can safely cast and add all song IDs to our list.
                songIds.addAll((Set<String>) entry.getValue());
            }
            // Now call the constructor with both arguments.
            playlistList.add(new PlaylistModel(entry.getKey(), songIds));
        }

        // Sort the list alphabetically
        Collections.sort(playlistList, (p1, p2) -> p1.getName().compareTo(p2.getName()));

        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }
}


// =====================================================================================
// NEW PLAYLIST ADAPTER CLASS (You can put this in its own file or as an inner class)
// =====================================================================================

class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final Context context;
    private final List<PlaylistModel> playlistList;
    private final OnPlaylistClickListener listener;

    // Interface for click events
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
        // You must create a layout file named 'item_playlist.xml' for this to work.
        // I have provided the XML for it in previous responses.
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistModel playlist = playlistList.get(position);
        holder.playlistName.setText(playlist.getName());

        // Set the icon for the playlist
        holder.playlistIcon.setImageResource(R.drawable.icon_playlist); // Make sure you have 'ic_playlist' in your drawables

        // Handle the click event
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
