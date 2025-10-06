# MusicPlayerApp

**MusicPlayerApp** is a modern Android music player built with **Java** and configured using **Groovy (Gradle scripts)**. The app provides a seamless music experience with support for **online streaming**, **offline playback**, **playlist management**, and **downloads**, allowing users to enjoy their favorite music anywhere, anytime.

---

## Features

- **Online Streaming** – Stream music from online sources with minimal buffering.  
- **Offline Playback** – Download music tracks to play without an internet connection.  
- **Playlist Management** – Create, edit, and manage custom playlists.  
- **Background Playback** – Music continues playing even when the app is minimized.  
- **Media Controls** – Play, pause, skip, and seek through tracks easily.  
- **Search Functionality** – Quickly find songs in your library or online catalog.  
- **Intuitive UI** – Clean and responsive interface for smooth navigation.  
- **Download Management** – Monitor and manage downloaded tracks efficiently.

---

## Technologies Used

- **Programming Language:** Java  
- **Build System:** Gradle (Groovy scripts)  
- **Android SDK Version:** [e.g., 34]  
- **Libraries & Dependencies:**  
  - Material Design: `com.google.android.material:material:1.11.0`  
  - AndroidX Core: `androidx.core:core-ktx:1.12.0`
  - etc
---

## Android Project Types

MusicPlayerApp is an **Android Application Module**, not a library:

1. **Application**
   - Plugin: `id 'com.android.application'`  
   - Produces a full app that can be installed on Android devices (APK/AAB).  
   - Includes `applicationId` in `build.gradle`.  
   - Example: The main MusicPlayerApp installed by users.

2. **Library**
   - Plugin: `id 'com.android.library'`  
   - Produces a reusable library (.aar) that other apps can use.  
   - No `applicationId` is needed.  
   - Example: Modules for shared UI components or utility functions.

> Think of it like this: Application = a finished car. Library = an engine that can be installed in any car.

---

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/MusicPlayerApp.git
   cd MusicPlayerApp
