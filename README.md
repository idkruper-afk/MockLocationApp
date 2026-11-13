# Android Mock Location App

A ready-to-compile Android project that spoofs both `GPS_PROVIDER` and `NETWORK_PROVIDER` coordinates using a Foreground Service so the location remains locked even when the app is minimized.

## Option 1: Build in Android Studio
1. Extract this ZIP archive.
2. Open **Android Studio** -> **Open** and select the `MockLocationApp` folder.
3. Let Gradle sync, connect your Android phone via USB, and click **Run (▶)**.

## Option 2: Build APK in the Cloud (No Android Studio Required)
1. Upload these extracted files to a free GitHub repository.
2. Go to the **Actions** tab in your repository and run the **Build Android APK** workflow.
3. Download the compiled `app-debug.apk` from the workflow artifacts and install it on your phone.
