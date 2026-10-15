package com.luma.launcher

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFF171A35.toInt()),
            navigationBarStyle = SystemBarStyle.dark(0xFF171A35.toInt()),
        )
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFB7C5FF),
                    onPrimary = Color(0xFF172047),
                    secondary = Color(0xFFFFD3BC),
                    background = Color(0xFF171A35),
                    surface = Color(0xFF252A4D),
                    onSurface = Color.White,
                ),
            ) {
                LumaLauncher()
            }
        }
    }
}

private enum class Destination {
    HOME,
    APP_DRAWER,
    SETTINGS,
    LOCK_PREVIEW,
}

private enum class WallpaperTarget(val label: String, val flags: Int) {
    HOME("Home screen", WallpaperManager.FLAG_SYSTEM),
    LOCK("Lock screen", WallpaperManager.FLAG_LOCK),
    BOTH("Both screens", WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK),
}

@Composable
private fun LumaLauncher() {
    val context = LocalContext.current
    val preferences = remember { LauncherPreferences(context) }

    var destination by remember { mutableStateOf(Destination.HOME) }
    var animationsEnabled by remember { mutableStateOf(preferences.animationsEnabled) }
    var labelsEnabled by remember { mutableStateOf(preferences.labelsEnabled) }
    var wallpaperUri by remember { mutableStateOf(preferences.homeWallpaperUri) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var pendingWallpaperTarget by remember { mutableStateOf(WallpaperTarget.BOTH) }

    val apps by produceState(initialValue = emptyList<LauncherAppEntry>()) {
        value = withContext(Dispatchers.IO) { loadLauncherApps(context) }
    }

    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Some document providers grant a temporary URI instead.
            }

            try {
                val stream = context.contentResolver.openInputStream(uri)
                    ?: error("The selected image could not be opened.")
                stream.use {
                    WallpaperManager.getInstance(context).setStream(
                        it,
                        null,
                        true,
                        pendingWallpaperTarget.flags,
                    )
                }
                if (pendingWallpaperTarget != WallpaperTarget.LOCK) {
                    wallpaperUri = uri.toString()
                    preferences.homeWallpaperUri = wallpaperUri
                }
                Toast.makeText(context, "Wallpaper updated", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                Toast.makeText(
                    context,
                    error.message ?: "Could not set this wallpaper.",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    BackHandler(enabled = destination != Destination.HOME) {
        destination = if (destination == Destination.LOCK_PREVIEW) {
            Destination.SETTINGS
        } else {
            Destination.HOME
        }
    }

    if (showWallpaperDialog) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = { Text("Set wallpaper") },
            text = { Text("Choose which screen should use your photo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWallpaperDialog = false
                        pendingWallpaperTarget = WallpaperTarget.BOTH
                        wallpaperPicker.launch(arrayOf("image/*"))
                    },
                ) { Text(WallpaperTarget.BOTH.label) }
            },
            dismissButton = {
                androidx.compose.foundation.layout.Row {
                    TextButton(
                        onClick = {
                            showWallpaperDialog = false
                            pendingWallpaperTarget = WallpaperTarget.HOME
                            wallpaperPicker.launch(arrayOf("image/*"))
                        },
                    ) { Text("Home") }
                    TextButton(
                        onClick = {
                            showWallpaperDialog = false
                            pendingWallpaperTarget = WallpaperTarget.LOCK
                            wallpaperPicker.launch(arrayOf("image/*"))
                        },
                    ) { Text("Lock") }
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = true),
        )
    }

    val transitions = if (animationsEnabled) {
        (slideInHorizontally { it / 5 } + fadeIn()).togetherWith(
            slideOutHorizontally { -it / 5 } + fadeOut(),
        )
    } else {
        fadeIn() togetherWith fadeOut()
    }

    AnimatedContent(
        targetState = destination,
        transitionSpec = { transitions },
        label = "launcher-screen",
    ) { screen ->
        when (screen) {
            Destination.HOME -> HomeScreen(
                apps = apps,
                showLabels = labelsEnabled,
                animationsEnabled = animationsEnabled,
                wallpaperUri = wallpaperUri,
                onOpenSettings = { destination = Destination.SETTINGS },
                onOpenDrawer = { destination = Destination.APP_DRAWER },
                onChooseWallpaper = { showWallpaperDialog = true },
                onLaunchApp = { launchApp(context, it) },
            )

            Destination.APP_DRAWER -> AppDrawerScreen(
                apps = apps,
                onBack = { destination = Destination.HOME },
                onLaunchApp = { launchApp(context, it) },
            )

            Destination.SETTINGS -> SettingsScreen(
                animationsEnabled = animationsEnabled,
                showLabels = labelsEnabled,
                onAnimationsChanged = {
                    animationsEnabled = it
                    preferences.animationsEnabled = it
                },
                onLabelsChanged = {
                    labelsEnabled = it
                    preferences.labelsEnabled = it
                },
                onChooseWallpaper = { showWallpaperDialog = true },
                onShowLockPreview = { destination = Destination.LOCK_PREVIEW },
                onBack = { destination = Destination.HOME },
            )

            Destination.LOCK_PREVIEW -> LockScreenPreview(
                onBack = { destination = Destination.SETTINGS },
            )
        }
    }
}
