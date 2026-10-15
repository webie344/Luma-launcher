package com.luma.launcher

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    apps: List<LauncherAppEntry>,
    showLabels: Boolean,
    animationsEnabled: Boolean,
    wallpaperUri: String?,
    onOpenSettings: () -> Unit,
    onOpenDrawer: () -> Unit,
    onChooseWallpaper: () -> Unit,
    onLaunchApp: (LauncherAppEntry) -> Unit,
) {
    val context = LocalContext.current
    val customWallpaper by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = wallpaperUri,
    ) {
        value = if (wallpaperUri == null) {
            null
        } else {
            withContext(Dispatchers.IO) { loadWallpaperBitmap(context, wallpaperUri) }
        }
    }

    val pageSize = 16
    val pageCount = ceil(apps.size.coerceAtLeast(1).toDouble() / pageSize).toInt()
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(15_000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (customWallpaper != null) {
            Image(
                bitmap = customWallpaper!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101326).copy(alpha = 0.24f)),
            )
        } else {
            LumaWallpaper()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "LUMA",
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp,
                )
                Row {
                    IconButton(onClick = onChooseWallpaper) {
                        Icon(
                            Icons.Default.Wallpaper,
                            contentDescription = "Choose wallpaper",
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                    color = Color.White,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-3).sp,
                    lineHeight = 82.sp,
                )
                Text(
                    text = now.format(
                        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()),
                    ),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            SearchPill(onClick = onOpenDrawer)
            Spacer(Modifier.height(15.dp))

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Your installed apps will appear here",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    key = { it },
                ) { page ->
                    val pageApps = apps.drop(page * pageSize).take(pageSize)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false,
                    ) {
                        items(pageApps, key = LauncherAppEntry::packageName) { app ->
                            AppIcon(
                                app = app,
                                showLabel = showLabels,
                                animationsEnabled = animationsEnabled,
                                onClick = { onLaunchApp(app) },
                            )
                        }
                    }
                }
            }

            if (pageCount > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(pageCount) { page ->
                        val color by animateColorAsState(
                            targetValue = if (pagerState.currentPage == page) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.38f)
                            },
                            label = "page-indicator",
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (pagerState.currentPage == page) 7.dp else 5.dp)
                                .background(color, CircleShape),
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }

            val dockApps = remember(apps) { chooseDockApps(apps) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dockApps.forEach { app ->
                    AppIcon(
                        app = app,
                        showLabel = false,
                        animationsEnabled = animationsEnabled,
                        dock = true,
                        onClick = { onLaunchApp(app) },
                    )
                }
            }

            Text(
                text = "Swipe between pages  ·  Tap search for all apps",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LumaWallpaper() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6278C8),
                        Color(0xFF8A82C7),
                        Color(0xFFD09AAB),
                        Color(0xFF394B7C),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 5.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFD9C6).copy(alpha = 0.8f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.BottomStart)
                .padding(start = 0.dp, bottom = 55.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF6AC6D6).copy(alpha = 0.58f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.06f), Color.Black.copy(alpha = 0.27f)),
                    ),
                ),
        )
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.86f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF5A6072))
        Spacer(Modifier.width(10.dp))
        Text(
            "Search apps",
            color = Color(0xFF676B78),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Text("⌘", color = Color(0xFF777C8D), fontSize = 15.sp)
    }
}

@Composable
private fun AppIcon(
    app: LauncherAppEntry,
    showLabel: Boolean,
    animationsEnabled: Boolean,
    dock: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (animationsEnabled && pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "app-icon-press",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(if (dock) 48.dp else 57.dp)
                .clip(RoundedCornerShape(if (dock) 15.dp else 17.dp)),
        )
        if (showLabel && !dock) {
            Text(
                text = app.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = Color.White,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun chooseDockApps(apps: List<LauncherAppEntry>): List<LauncherAppEntry> {
    if (apps.isEmpty()) return emptyList()
    val preferred = listOf(
        listOf("phone", "dialer"),
        listOf("message", "sms"),
        listOf("chrome", "browser", "internet"),
        listOf("camera"),
    )
    val chosen = preferred.mapNotNull { terms ->
        apps.firstOrNull { app ->
            terms.any { term ->
                app.label.contains(term, ignoreCase = true) ||
                    app.packageName.contains(term, ignoreCase = true)
            }
        }
    }.distinctBy(LauncherAppEntry::packageName).toMutableList()

    apps.forEach { app ->
        if (chosen.size < 4 && chosen.none { it.packageName == app.packageName }) {
            chosen.add(app)
        }
    }
    return chosen.take(4)
}

@Composable
fun AppDrawerScreen(
    apps: List<LauncherAppEntry>,
    onBack: () -> Unit,
    onLaunchApp: (LauncherAppEntry) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(apps, query) {
        if (query.isBlank()) apps
        else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
                Text(
                    "App Library",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Find an app",
            )
            Spacer(Modifier.height(14.dp))
            if (filteredApps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No apps found", color = Color.White.copy(alpha = 0.8f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(filteredApps, key = LauncherAppEntry::packageName) { app ->
                        AppIcon(app, showLabel = true, animationsEnabled = true) {
                            onLaunchApp(app)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    animationsEnabled: Boolean,
    showLabels: Boolean,
    onAnimationsChanged: (Boolean) -> Unit,
    onLabelsChanged: (Boolean) -> Unit,
    onChooseWallpaper: () -> Unit,
    onShowLockPreview: () -> Unit,
    onBack: () -> Unit,
) {
    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
                Text(
                    "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingsSectionTitle("PERSONALIZE")
                SettingsRow(
                    icon = Icons.Default.Wallpaper,
                    title = "Wallpaper",
                    subtitle = "Set a photo on Home, Lock, or both",
                    onClick = onChooseWallpaper,
                )
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Lock screen preview",
                    subtitle = "Preview the iPhone-inspired clock style",
                    onClick = onShowLockPreview,
                )

                SettingsSectionTitle("HOME SCREEN")
                SettingsToggle(
                    title = "Smooth animations",
                    subtitle = "Use springy taps and screen transitions",
                    checked = animationsEnabled,
                    onCheckedChange = onAnimationsChanged,
                )
                SettingsToggle(
                    title = "App labels",
                    subtitle = "Show names below Home screen icons",
                    checked = showLabels,
                    onCheckedChange = onLabelsChanged,
                )

                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "About the Android lock screen",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Android keeps control of its secure lock screen, notifications, and unlock method. Luma can set its wallpaper, but it cannot replace that system screen.",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }

                Text(
                    "Luma Launcher  ·  iPhone-inspired, made for Android",
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun LockScreenPreview(onBack: () -> Unit) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(15_000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LumaWallpaper()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
            }
            Spacer(Modifier.weight(0.8f))
            Text(
                text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                color = Color.White,
                fontSize = 92.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-4).sp,
                lineHeight = 100.sp,
            )
            Spacer(Modifier.height(24.dp))
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    "Lock screen style preview",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Color.White,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Preview only · Android still handles secure unlock",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
        color = Color.White.copy(alpha = 0.67f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFD8DFFF))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF52586A))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color(0xFF757B89), fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF252A3B)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ScreenBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        LumaWallpaper()
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF13172F).copy(alpha = 0.72f)),
        )
        content()
    }
}
