# MonoIcons

MonoIcons is an LSPosed module for Samsung One UI Home. It applies a grayscale color filter when the launcher loads an app icon, including icons for apps installed later. It keeps the Samsung launcher and home-screen layout.

## Requirements

- A rooted Samsung device
- Magisk
- An LSPosed build that supports libxposed API 102
- Samsung One UI Home package `com.sec.android.app.launcher`

This is not a normal launcher APK. Without root and LSPosed, Android does not let a regular app recolor icons in One UI Home. This APK does not unlock the bootloader or root the phone.

## Install

1. Install `MonoIcons-debug.apk`.
2. Open LSPosed and enable MonoIcons. Its declared scope is One UI Home.
3. Restart the phone or One UI Home.
4. If existing icons remain colored, restart One UI Home so it reloads the cached icons.

If the phone is not rooted, stop before unlocking the bootloader: Samsung bootloader unlocking can erase user data and affect Knox state. The module has not been tested on the user's specific firmware.

## Build

Open this repository in Android Studio, or run `gradle --no-daemon :app:assembleDebug` with Android SDK 35 and Java 17. GitHub Actions builds the debug APK for the main branch and this feature branch.
