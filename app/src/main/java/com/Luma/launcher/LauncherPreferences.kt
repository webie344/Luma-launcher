package com.luma.launcher

import android.content.Context

class LauncherPreferences(context: Context) {
    private val store = context.applicationContext
        .getSharedPreferences("luma_launcher_preferences", Context.MODE_PRIVATE)

    var animationsEnabled: Boolean
        get() = store.getBoolean(KEY_ANIMATIONS, true)
        set(value) {
            store.edit().putBoolean(KEY_ANIMATIONS, value).apply()
        }

    var labelsEnabled: Boolean
        get() = store.getBoolean(KEY_LABELS, true)
        set(value) {
            store.edit().putBoolean(KEY_LABELS, value).apply()
        }

    var homeWallpaperUri: String?
        get() = store.getString(KEY_HOME_WALLPAPER, null)
        set(value) {
            store.edit().putString(KEY_HOME_WALLPAPER, value).apply()
        }

    private companion object {
        const val KEY_ANIMATIONS = "animations_enabled"
        const val KEY_LABELS = "app_labels_enabled"
        const val KEY_HOME_WALLPAPER = "home_wallpaper_uri"
    }
}
