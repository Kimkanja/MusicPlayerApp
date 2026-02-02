package com.kimani.musicplayerapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * NetworkUtils provides helper methods to check the device's network connectivity status.
 */
public class NetworkUtils {

    /**
     * Checks if the device is currently connected to the internet.
     * It handles different Android versions to ensure compatibility
     * and verifies that the internet connection is actually usable.
     *
     * @param context The application or activity context.
     * @return True if internet is available, false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        // Return false if context is null
        if (context == null) {
            return false;
        }

        // Get the ConnectivityManager service to query network status
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        // For Android 6.0 (API 23) and above, use NetworkCapabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Get the currently active network
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return false;
            }

            // Get the capabilities of the active network
            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(activeNetwork);

            if (capabilities == null) {
                return false;
            }

            // Check if the network has internet capability
            // and that the connection has been validated (real internet access)
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        } else {
            // Fallback for older Android versions using the deprecated NetworkInfo
            android.net.NetworkInfo activeNetworkInfo =
                    connectivityManager.getActiveNetworkInfo();

            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
}
