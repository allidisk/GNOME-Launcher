package com.gnome.launcher.ui.home

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gnome.launcher.LauncherViewModel
import com.gnome.launcher.ui.GnomeColors
import com.gnome.launcher.ui.widgets.WidgetArea
import com.gnome.launcher.ui.widgets.WidgetPickerActivity
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val wallpaperLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

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

            // Pinned apps grid on home screen
            if (uiState.pinnedApps.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(uiState.pinnedApps, key = { it.packageName }) { app ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.launchApp(app.packageName) }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Image(
                                painter = DrawablePainter(app.icon),
                                contentDescription = app.label,
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                text = app.label,
                                color = GnomeColors.TextPrimary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // Empty state hint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Long-press apps in the drawer\nto pin them here",
                        color = GnomeColors.TextDisabled,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            GnomeDock(
                apps = uiState.pinnedApps.take(5),
                onAppClick = { viewModel.launchApp(it) },
                onUnpinApp = { viewModel.unpinApp(it) },
                onAppDrawerClick = onOpenDrawer,
                onWallpaperClick = {
                    val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                    wallpaperLauncher.launch(Intent.createChooser(intent, "Select Wallpaper"))
                }
            )
        }
    }
}
