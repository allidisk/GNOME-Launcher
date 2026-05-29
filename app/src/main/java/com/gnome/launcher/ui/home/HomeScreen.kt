package com.gnome.launcher.ui.home

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gnome.launcher.LauncherViewModel
import com.gnome.launcher.ui.GnomeColors
import com.gnome.launcher.ui.widgets.WidgetArea
import com.gnome.launcher.ui.widgets.WidgetPickerActivity

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Wallpaper picker
    val wallpaperLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* handled by system */ }

    // Widget picker: receives the final widget ID after pick + optional config
    val widgetPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val widgetId = result.data?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                viewModel.addWidget(widgetId)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GnomeColors.Background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -80f) onOpenDrawer()
                }
            }
    ) {
        // Wallpaper gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GnomeColors.Background.copy(alpha = 0.3f))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            GnomeTopBar(onActivitiesClick = onOpenDrawer)

            // Widget area — fully wired with real IDs and callbacks
            WidgetArea(
                widgetIds = uiState.widgetIds,
                onAddWidgetClick = {
                    val intent = Intent(context, WidgetPickerActivity::class.java)
                    widgetPickerLauncher.launch(intent)
                },
                onRemoveWidget = { viewModel.removeWidget(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )

            // Dock — uses persisted pinned apps
            GnomeDock(
                apps = uiState.pinnedApps,
                onAppClick = { viewModel.launchApp(it) },
                onUnpinApp = { viewModel.unpinApp(it) },
                onAppDrawerClick = onOpenDrawer,
                onWallpaperClick = {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                        .takeIf { context.packageManager.resolveActivity(it, 0) != null }
                        ?: Intent(Intent.ACTION_SET_WALLPAPER)
                    wallpaperLauncher.launch(intent)
                }
            )
        }
    }
}
