package com.kimani.musicplayerapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * NetworkUtils provides helper methods to check the device's network connectivity status.
 */
public class NetworkUtils {

    /**
     * Checks if the device is currently connected to the internet.
     * It handles different Android versions to ensure compatibility.
     *
     * @param context The application or activity context.
     * @return True if internet is available, false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        // Get the ConnectivityManager service to query network status
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        // For Android 10 (API 29) and above, use NetworkCapabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities == null) {
                return false;
            }
            // Check if the network has the INTERNET capability
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            // Fallback for older Android versions using the deprecated NetworkInfo
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
}
