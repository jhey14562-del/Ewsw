# Offline Runner (Native Android)

A small native Android endless-runner game written in Kotlin. Gameplay is local and requires no internet permission or backend.

## Features
- Tap to start, tap while running to jump
- Random obstacles and collision/game-over detection
- Increasing running speed and distance score
- Best score saved on-device with SharedPreferences
- Portrait, full-screen, touch-friendly game view
- GitHub Actions workflow that builds and uploads a debug APK

## Build with GitHub Actions
1. Push the complete project to the repository's `main` or `master` branch.
2. Open the **Actions** tab and choose **Build Offline Runner APK**.
3. Wait for the run to finish successfully.
4. Open the run and download the `offline-runner-debug-apk` artifact. Extract it to get `app-debug.apk`.

You can also manually start it from Actions using **Run workflow**.

## Build locally
Requires JDK 17, Android SDK platform 35, and Gradle 8.9:

```sh
gradle assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

This is a debug-signed APK for testing, not a Play Store release-signed APK.
