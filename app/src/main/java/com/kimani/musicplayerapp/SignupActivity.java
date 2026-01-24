package com.kimani.musicplayerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.kimani.musicplayerapp.databinding.ActivitySignupBinding;
import java.util.regex.Pattern;

/**
 * SignupActivity facilitates the creation of new user accounts.
 * It provides input validation for email and password, checks for internet connectivity,
 * and uses Firebase Authentication to register users.
 */
public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Listener for the "Create Account" button
        binding.createAccountBtn.setOnClickListener(v -> {
            // Validate internet connection before attempting signup
            if (!NetworkUtils.isNetworkAvailable(this)) {
                showNoInternetDialog();
                return;
            }

            // Access input fields using casting to TextInputEditText to ensure getText() is available
            TextInputEditText emailEditText = (TextInputEditText) binding.emailEdittext;
            TextInputEditText passwordEditText = (TextInputEditText) binding.passwordEdittext;
            TextInputEditText confirmPasswordEditText = (TextInputEditText) binding.confirmPasswordEdittext;

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // Perform input validation
            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                emailEditText.setError("Invalid email format");
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Password should be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords do not match");
                return;
            }

            // Proceed with Firebase registration
            createAccountWithFirebase(email, password);
        });

        // Navigate back to login
        binding.gotoLoginBtn.setOnClickListener(v -> finish());
    }

    /**
     * Attempts to create a new user account in Firebase.
     * @param email The user's email address.
     * @param password The user's chosen password.
     */
    private void createAccountWithFirebase(String email, String password) {
        setInProgress(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setInProgress(false);
                    Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to LoginActivity
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(getApplicationContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Toggles the UI state between 'in progress' (showing spinner) and 'idle' (showing button).
     * @param inProgress True if an operation is ongoing, false otherwise.
     */
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.createAccountBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.createAccountBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Displays a dialog informing the user that an internet connection is required.
     */
    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need an internet connection to sign up. Please check your network and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
