package com.kimani.musicplayerapp;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import com.kimani.musicplayerapp.databinding.ActivityLoginBinding;

import java.util.regex.Pattern;

/**
 * LoginActivity handles user authentication via Firebase.
 * It validates user input and checks for network connectivity before attempting to sign in.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using View Binding to access UI elements
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup the login button click listener
        binding.loginBtn.setOnClickListener(v -> {
            // Check for internet connection before proceeding
            if (!NetworkUtils.isNetworkAvailable(this)) {
                showNoInternetDialog();
                return;
            }

            // Retrieve email and password from text inputs
            TextInputEditText emailEditText = (TextInputEditText) binding.emailEdittext;
            TextInputEditText passwordEditText = (TextInputEditText) binding.passwordEdittext;

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            // Validate email format
            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                emailEditText.setError("Invalid email");
                return;
            }

            // Validate password length
            if (password.length() < 6) {
                passwordEditText.setError("Length should be 6 characters");
                return;
            }

            // Proceed with Firebase authentication
            loginWithFirebase(email, password);
        });

        // Navigate to the SignupActivity
        binding.gotoSignupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Attempts to sign in to Firebase with the provided credentials.
     *
     * @param email    User's email address.
     * @param password User's password.
     */
    private void loginWithFirebase(String email, String password) {
        setInProgress(true);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setInProgress(false);
                    // Redirect to SplashActivity on successful login to handle routing
                    startActivity(new Intent(LoginActivity.this, SplashActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(getApplicationContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If a user is already signed in, skip the login screen
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, SplashActivity.class));
            finish();
        }
    }

    /**
     * Toggles the UI state between 'Login' and 'In Progress' (showing a loader).
     *
     * @param inProgress True if an authentication task is running.
     */
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.loginBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.loginBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Displays a dialog informing the user that an internet connection is required.
     */
    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need an internet connection to log in. Please check your network and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
