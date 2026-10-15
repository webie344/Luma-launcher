package com.luma.launcher

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class LauncherAppEntry(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
)

@Suppress("DEPRECATION")
fun loadLauncherApps(context: Context): List<LauncherAppEntry> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return packageManager.queryIntentActivities(launcherIntent, 0)
        .mapNotNull { resolved ->
            val activityInfo = resolved.activityInfo ?: return@mapNotNull null
            if (activityInfo.packageName == context.packageName) return@mapNotNull null
            try {
                LauncherAppEntry(
                    packageName = activityInfo.packageName,
                    label = resolved.loadLabel(packageManager)?.toString()
                        ?.takeIf(String::isNotBlank)
                        ?: activityInfo.packageName,
                    icon = resolved.loadIcon(packageManager).toImageBitmap(),
                )
            } catch (_: Exception) {
                null
            }
        }
        .distinctBy(LauncherAppEntry::packageName)
        .sortedBy { it.label.lowercase() }
}

fun launchApp(context: Context, app: LauncherAppEntry) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        ?: return
    context.startActivity(launchIntent)
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    val width = intrinsicWidth.coerceIn(1, 512)
    val height = intrinsicHeight.coerceIn(1, 512)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}

fun loadWallpaperBitmap(context: Context, uriString: String): ImageBitmap? {
    return try {
        val uri = Uri.parse(uriString)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > 1800 ||
            bounds.outHeight / sampleSize > 1800
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}
