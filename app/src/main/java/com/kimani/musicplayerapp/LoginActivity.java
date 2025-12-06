// Replace this file: app/src/main/java/com/kimani/musicplayerapp/LoginActivity.java
package com.kimani.musicplayerapp;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText; // Import the correct class
import com.google.firebase.auth.FirebaseAuth;

import com.kimani.musicplayerapp.databinding.ActivityLoginBinding;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Login button click
        binding.loginBtn.setOnClickListener(v -> {
            // First, check for internet connection
            if (!NetworkUtils.isNetworkAvailable(this)) {
                showNoInternetDialog();
                return; // Stop the process
            }

            // --- FIX ---
            // Cast the views to TextInputEditText to access the getText() method.
            TextInputEditText emailEditText = (TextInputEditText) binding.emailEdittext;
            TextInputEditText passwordEditText = (TextInputEditText) binding.passwordEdittext;

            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                emailEditText.setError("Invalid email");
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Length should be 6 characters");
                return;
            }

            loginWithFirebase(email, password);
        });

        // Go to signup
        binding.gotoSignupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginWithFirebase(String email, String password) {
        setInProgress(true);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setInProgress(false);
                    startActivity(new Intent(LoginActivity.this, SplashActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, SplashActivity.class));
            finish();
        }
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.loginBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.loginBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need an internet connection to log in. Please check your network and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
