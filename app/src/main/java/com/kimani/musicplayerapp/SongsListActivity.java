package com.kimani.musicplayerapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import com.kimani.musicplayerapp.Adapter.SongsListAdapter;
import com.kimani.musicplayerapp.databinding.ActivitySongsListBinding;
import com.kimani.musicplayerapp.models.CategoryModel;

/**
 * SongsListActivity displays a list of songs belonging to a specific category.
 * It receives the category data via a static field and populates the UI with 
 * the category's cover image, name, and its list of songs.
 */
public class SongsListActivity extends AppCompatActivity {

    /**
     * The category whose songs are to be displayed.
     * This is set statically before launching the activity (Kotlin companion object style).
     */
    public static CategoryModel category;

    private ActivitySongsListBinding binding;
    private SongsListAdapter songsListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if category is not null before using it to prevent potential crashes.
        if (category != null) {
            // Set the category name in the title text view
            binding.nameTextView.setText(category.getName());
            
            // Load the category cover image using Glide with rounded corners
            Glide.with(binding.coverImageView)
                    .load(category.getCoverUrl())
                    .apply(new RequestOptions().transform(new RoundedCorners(32)))
                    .into(binding.coverImageView);

            // Initialize the list of songs
            setupSongsListRecyclerView();
        }
    }

    /**
     * Initializes the RecyclerView for displaying the list of songs in the category.
     */
    private void setupSongsListRecyclerView() {
        // Create adapter with the list of songs from the current category
        songsListAdapter = new SongsListAdapter(category.getSongs());
        binding.songsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.songsListRecyclerView.setAdapter(songsListAdapter);
    }
}
