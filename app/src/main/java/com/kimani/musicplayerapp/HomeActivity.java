// Replace this file: app/src/main/java/com/kimani/musicplayerapp/HomeActivity.java
package com.kimani.musicplayerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kimani.musicplayerapp.Adapter.CategoryAdapter;
import com.kimani.musicplayerapp.Adapter.SectionSongListAdapter;
import com.kimani.musicplayerapp.databinding.ActivityHomeBinding;
import com.kimani.musicplayerapp.models.CategoryModel;
import com.kimani.musicplayerapp.models.SongModel;

import java.util.List;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check for internet connection before loading data
        if (NetworkUtils.isNetworkAvailable(this)) {
            loadDataFromFirebase();
        } else {
            showNoInternetDialog();
        }

        setupBottomNavigation();
        setupLogoutButton();
    }

    private void loadDataFromFirebase() {
        getCategories();
        setupSection("section_1", binding.section1MainLayout, binding.section1Title, binding.section1RecyclerView);
        setupSection("section_2", binding.section2MainLayout, binding.section2Title, binding.section2RecyclerView);
        setupSection("section_3", binding.section3MainLayout, binding.section3Title, binding.section3RecyclerView);
        setupMostlyPlayed("mostly_played", binding.mostlyPlayedMainLayout, binding.mostlyPlayedTitle, binding.mostlyPlayedRecyclerView);
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need an internet connection to access online content. Please connect and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                    // Check again when Retry is clicked
                    if (NetworkUtils.isNetworkAvailable(HomeActivity.this)) {
                        loadDataFromFirebase(); // If connected, load the data
                    } else {
                        showNoInternetDialog(); // Otherwise, show the dialog again
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
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    return true;

                case R.id.bottom_playlist:
                    // Stop the currently playing online song
                    if (MyExoplayer.getInstance() != null) {
                        MyExoplayer.getInstance().stop();
                    }
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;

                case R.id.bottom_online:
                    startActivity(new Intent(getApplicationContext(), OnlineActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_profile:
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
    }

    public void showPlayerView() {
        binding.playerView.setOnClickListener(v -> {
            startActivity(new Intent(this, OnlinePlayerActivity.class));
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
                .addOnSuccessListener(sectionDocumentSnapshot -> {
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
                            });
                });
    }
}
