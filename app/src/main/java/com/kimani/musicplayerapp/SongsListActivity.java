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

public class SongsListActivity extends AppCompatActivity {

    // The 'companion object' in Kotlin is replaced by a 'public static' field in Java.
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
            binding.nameTextView.setText(category.getName());
            Glide.with(binding.coverImageView)
                    .load(category.getCoverUrl())
                    .apply(new RequestOptions().transform(new RoundedCorners(32)))
                    .into(binding.coverImageView);

            setupSongsListRecyclerView();
        }
    }

    private void setupSongsListRecyclerView() {
        songsListAdapter = new SongsListAdapter(category.getSongs());
        binding.songsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.songsListRecyclerView.setAdapter(songsListAdapter);
    }
}
