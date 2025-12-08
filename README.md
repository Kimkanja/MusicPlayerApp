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
* **Etc:** Additional enhancements and new features based on user feedback.

---

## App Screenshots

Below are some screenshots demonstrating the interface and features:

<p><img src="screenshots/login.png" width="320"/></p>
*Login screen shows up for users with registered accounts.*

<p><img src="screenshots/register.png" width="320"/></p>
*Register screen for new users.*

<p><img src="screenshots/splash.png" width="320"/></p>
*Splash screen welcoming the user.*

<p><img src="screenshots/home.png" width="320"/></p>
*Home screen showing playlists and navigation.*

<p><img src="screenshots/player1.jpg" width="320"/></p>
*Offline player screen with cover image.*

<p><img src="screenshots/player2.png" width="320"/></p>
*Player screen with waveform and no cover image.*

<p><img src="screenshots/search.png" width="320"/></p>
*Future search feature mockup.*

<p><img src="screenshots/main.png" width="320"/></p>
*Offline playlist screen with available songs.*

<p><img src="screenshots/playlist.png" width="320"/></p>
*User-created custom playlists screen.*

<p><img src="screenshots/lockscreen.png" width="320"/></p>
*Phone lock screen showing ongoing playback.*

<p><img src="screenshots/notification.png" width="320"/></p>
*Notification controls while music is playing.*

<p><img src="screenshots/online_player.png" width="320"/></p>
*Online streaming player interface.*

---

## Technologies and Tools

* **Programming Language:** Java
* **Build System:** Gradle (Groovy)
* **Android SDK:** 36 (min SDK 24, target SDK 36)
* **UI/UX:** Material Design Components
* **Libraries & Dependencies:**
    * Firebase Authentication and Firestore
    * ExoPlayer (Media3)
    * Glide + Transformations
    * WaveformSeekBar
    * RecyclerView, Palette, and other AndroidX libraries

---

## Project Structure and Module Types

**Application Module**
* Uses `com.android.application` plugin
* Produces full APK/AAB for installation

**Library Module (optional)**
* Uses `com.android.library` plugin
* Provides reusable components

---

## Setup Instructions

```bash
git clone https://github.com/your-username/MusicPlayerApp.git
cd MusicPlayerApp
git