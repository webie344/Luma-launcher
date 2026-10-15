# Luma Launcher

A native Android home-screen replacement with an iPhone-inspired layout, app library, wallpaper controls, and smooth launcher transitions. This is an Android Studio project, not a website; it contains no HTML.

## Open and build

1. Install Android Studio and its Android SDK Platform 35.
2. Open the `iphone-style-launcher` folder in Android Studio.
3. Use JDK 17 when prompted and let Gradle sync.
4. Run the app on an Android phone or emulator.
5. On the phone, press Home and choose **Luma Launcher**. You can also select it under Android Settings → Apps → Default apps → Home app.
6. To create an APK, use **Build → Build Bundle(s) / APK(s) → Build APK(s)** in Android Studio.

The first run needs network access so Gradle can download the Android and Compose dependencies.

## Included

- A two-tone iPhone-inspired home layout with clock, app pages, search, app library, and dock.
- Real installed-app icons and launch actions.
- Photo wallpaper picker for Home, Lock, or Both. Home photos also appear behind the launcher UI.
- Settings for labels and launcher animations, plus a lock-screen-style preview.
- Native Android Compose screens; no web server or HTML is required.

## Android platform limits

Luma can be selected as the default Home app and can change the Home/Lock wallpaper. Android retains the secure lock screen, lock-screen notifications, and unlock flow; the in-app lock screen is only a visual preview. Animations apply to Luma's own screens and icons, not Android system animations.

This project is unsigned source code. Android Studio can produce a debug APK for testing. A release APK intended for distribution needs to be signed with your own signing key.
