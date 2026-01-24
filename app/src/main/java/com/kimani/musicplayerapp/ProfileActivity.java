package com.kimani.musicplayerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ProfileActivity manages the user's profile information.
 * It displays user details from Firebase Auth, handles account logout,
 * and manages bottom navigation between the main sections of the app.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userEmailTextView;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        userNameTextView = findViewById(R.id.user_name_text_view);
        userEmailTextView = findViewById(R.id.user_email_text_view);
        logoutBtn = findViewById(R.id.logout_btn);

        // Populate user data and set up interactions
        setupUserData();
        setupBottomNavigation();
        setupLogoutButton();
    }

    /**
     * Retrieves the current Firebase user and updates the UI with their email and display name.
     * If a display name isn't set, it derives a name from the email prefix.
     */
    private void setupUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Set user email
            userEmailTextView.setText(currentUser.getEmail());

            // Set user name (extract from email if display name is null)
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userNameTextView.setText(currentUser.getDisplayName());
            } else {
                String email = currentUser.getEmail();
                if (email != null && email.contains("@")) {
                    userNameTextView.setText(email.split("@")[0]);
                } else {
                    userNameTextView.setText("User");
                }
            }
        }
    }

    /**
     * Configures the logout button to sign the user out of Firebase,
     * stop any active music playback, and redirect to the LoginActivity.
     */
    private void setupLogoutButton() {
        logoutBtn.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Stop any playing music to prevent audio leakage after logout
            if (MyExoplayer.getInstance() != null) {
                MyExoplayer.getInstance().stop();
            }

            // Go to LoginActivity and clear the back stack so the user cannot navigate back to the profile
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Initializes the bottom navigation bar and handles switching between 
     * Home, Playlists, Online, and Profile sections.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_profile:
                    return true;
                case R.id.bottom_home:
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    return true;
                case R.id.bottom_playlist:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    return true;
                case R.id.bottom_online:
                    startActivity(new Intent(getApplicationContext(), OnlineActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    return true;
            }
            return false;
        });
    }
}
