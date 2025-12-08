# MusicPlayerApp

**MusicPlayerApp** is a modern and fully-featured Android music player application developed using **Java** and configured with **Gradle (Groovy scripts)**. The app offers a seamless music experience, supporting **online streaming**, **offline playback**, **playlist management**, and **downloads**, enabling users to enjoy their favorite music anytime, anywhere.

---

## Key Features

* **Online Streaming:** Stream music from online sources with minimal buffering and high quality.
* **Offline Playback:** Download and play music tracks without an internet connection.
* **Playlist Management:** Create, edit, and manage custom playlists for personalized listening.
* **Background Playback:** Music continues playing even when the app is minimized or the device is locked.
* **Media Controls:** Intuitive controls for play, pause, skip, and seek.
* **Intuitive UI/UX:** Clean, responsive, and user-friendly interface for smooth navigation.
* **Error Handling:** User-friendly messages and safeguards for network or API issues.
* **Testing:** Fully tested with JUnit and Espresso to ensure reliability and performance.

---

## Future Features

* **Search Functionality:** Quickly find songs in both local and online libraries.
* **Download Management:** Efficiently monitor and manage downloaded music tracks.
* **Etc:** Additional enhancements and new features to be implemented based on user feedback and evolving app requirements.

---

## App Screenshots

<p align="center">
  <img src="screenshots/login.png" width="200" />
  <img src="screenshots/register.png" width="200" />
</p>
<p align="center">
  <img src="screenshots/splash.png" width="200" />
  <img src="screenshots/home.png" width="200" />
</p>
<p align="center">
  <img src="screenshots/player1.jpg" width="200" />
  <img src="screenshots/player2.png" width="200" />
</p>
<p align="center">
  <img src="screenshots/search.png" width="200" />
  <img src="screenshots/main.png" width="200" />
</p>
<p align="center">
  <img src="screenshots/playlist.png" width="200" />
  <img src="screenshots/lockscreen.png" width="200" />
</p>
<p align="center">
  <img src="screenshots/notification.png" width="200" />
  <img src="screenshots/online_player.png" width="200" />
</p>

---

## Technologies and Tools

* **Programming Language:** Java
* **Build System:** Gradle (Groovy scripts)
* **Android SDK:** 36 (minimum SDK 24, target SDK 36)
* **UI/UX Framework:** Material Design Components
* **Libraries & Dependencies:**
    * Material Design: `com.google.android.material:material:1.11.0`
    * AndroidX Core: `androidx.core:core-ktx:1.12.0`
    * Firebase Authentication and Firestore
    * ExoPlayer (Media3) for advanced media playback
    * Glide for image loading and transformations
    * WaveformSeekBar for visual audio progress representation
    * RecyclerView, Palette, and other AndroidX libraries

---

## Project Structure and Module Types

**MusicPlayerApp** is structured as a complete **Android Application Module**:

1. **Application Module**
    * Plugin: `id 'com.android.application'`
    * Produces a full Android app (APK/AAB) that can be installed on devices.
    * Includes `applicationId` in `build.gradle`.
    * Example: The main MusicPlayerApp used by end-users.

2. **Library Module** (if applicable)
    * Plugin: `id 'com.android.library'`
    * Produces reusable components (.aar) for other apps.
    * No `applicationId` required.
    * Example: Shared UI components or utility modules.

> Think of it like this: Application = a finished car. Library = an engine that can be installed in any car.

---

## Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/MusicPlayerApp.git
   cd MusicPlayerApp
