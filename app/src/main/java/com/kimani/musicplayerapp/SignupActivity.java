// Replace this file: app/src/main/java/com/kimani/musicplayerapp/SignupActivity.java
package com.kimani.musicplayerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText; // Import the correct class
import com.google.firebase.auth.FirebaseAuth;
import com.kimani.musicplayerapp.databinding.ActivitySignupBinding;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.createAccountBtn.setOnClickListener(v -> {
            // First, check for internet connection
            if (!NetworkUtils.isNetworkAvailable(this)) {
                showNoInternetDialog();
                return; // Stop the process if no internet
            }

            // --- FIX ---
            // Cast the views from View to TextInputEditText to access getText()
            TextInputEditText emailEditText = (TextInputEditText) binding.emailEdittext;
            TextInputEditText passwordEditText = (TextInputEditText) binding.passwordEdittext;
            TextInputEditText confirmPasswordEditText = (TextInputEditText) binding.confirmPasswordEdittext;

            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                emailEditText.setError("Invalid email");
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Length should be 6 char");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Password not matched");
                return;
            }

            createAccountWithFirebase(email, password);
        });

        binding.gotoLoginBtn.setOnClickListener(v -> finish());
    }

    private void createAccountWithFirebase(String email, String password) {
        setInProgress(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setInProgress(false);
                    Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(getApplicationContext(), "Create account failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.createAccountBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.createAccountBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need an internet connection to sign up. Please check your network and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
