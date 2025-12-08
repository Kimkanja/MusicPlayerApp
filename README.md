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

Below are some screenshots of MusicPlayerApp demonstrating the interface and features:

![Login Screen](screenshots/login.png)
*Login screen shows up for users with already registered accounts to login*

![Register Screen](screenshots/register.png)
*Register screen shows up for first time user to create and register for a new account*

![Splash Screen Feature](screenshots/splash.png)
*Splash Screen shows a welcome message to user while accessing the MusicPlayerApp*

![Home Screen](screenshots/home.png)
*Home Screen showing playlists and navigation of online songs*

![Offline Player Screen](screenshots/player1.jpg)
*Player screen with play/pause controls and waveform with song having cover image*

![Player Screen](screenshots/player2.png)
*Player screen with play/pause controls and waveform with song that does not have cover image*

![Search Feature](screenshots/search.png)
*Future Search functionality UI preview*

![Playlist Screen](screenshots/main.png)
*Offline Playlist Screen showing playlist songs available on the phone and navigation*

![Custom Playlist Screen](screenshots/playlist.png)
*User Custom created playlist screen with all user created playlists*

![Phone Lock Screen](screenshots/lockscreen.png)
*This is lock Screen showing song playing continue... while the phone is on lock*

![Notification Screen](screenshots/notification.png)
*Notification screen showing song controls on the notification while its playing..*

![Online Player Screen](screenshots/online_player.png)
*Online Player screen with play/pause controls that shows song streaming online..*


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
   ```
2. **Open the project in Android Studio:**

    * Make sure Android Studio is updated to the latest version.
    * Sync the Gradle project.
3. **Build and Run:**

    * Connect an Android device or launch an emulator.
    * Run the application to explore features.

---

## Testing

* **Unit Testing:** Implemented with JUnit to validate core functionalities.
* **UI Testing:** Implemented with Espresso to verify user interactions and interface components.
* **Device Testing:** Verified on multiple devices and emulator configurations to ensure consistent performance and responsiveness.

---

## Professional Highlights

* Fully-featured music player with online and offline capabilities.
* Clean, modern, and responsive interface designed using Material Design.
* Robust error handling ensures a smooth user experience.
* Well-documented code and README for clarity and maintainability.
* Proper version control and project management using GitHub/GitLab.

---

This project demonstrates **practical Android development skills**, teamwork, and professional application structuring, making it suitable for academic presentations and portfolio showcases.
