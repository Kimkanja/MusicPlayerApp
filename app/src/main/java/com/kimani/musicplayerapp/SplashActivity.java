package com.kimani.musicplayerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.kimani.musicplayerapp.databinding.ActivitySplashBinding;

/**
 * SplashActivity serves as the initial entry point of the application.
 * It provides a welcoming screen and a starting point for users to navigate 
 * to the main functionality of the music player.
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable Edge-to-Edge display to utilize the full screen area
        EdgeToEdge.enable(this);
        
        // Use View Binding to inflate the layout and set the content view
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the click listener for the "Start" button
        // This button transitions the user from the splash screen to the MainActivity
        binding.startBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Intent to transition from SplashActivity to MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                
                // Optional: finish() could be called here if you don't want the user 
                // to return to the splash screen when pressing the back button.
            }
        });
    }
}
