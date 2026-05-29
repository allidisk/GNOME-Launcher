package com.gnome.launcher.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gnome.launcher.ui.GnomeColors

const val APPWIDGET_HOST_ID = 1337

@Composable
fun WidgetArea(
    widgetIds: List<Int>,
    onAddWidgetClick: () -> Unit,
    onRemoveWidget: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val widgetManager = remember { AppWidgetManager.getInstance(context) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        widgetIds.forEach { appWidgetId ->
            val info = remember(appWidgetId) { widgetManager.getAppWidgetInfo(appWidgetId) }
            if (info != null) {
                HostedWidget(
                    context = context,
                    widgetManager = widgetManager,
                    appWidgetId = appWidgetId,
                    info = info,
                    onRemove = { onRemoveWidget(appWidgetId) }
                )
            }
        }

        AddWidgetButton(onClick = onAddWidgetClick)
    }
}

@Composable
private fun HostedWidget(
    context: Context,
    widgetManager: AppWidgetManager,
    appWidgetId: Int,
    info: AppWidgetProviderInfo,
    onRemove: () -> Unit
) {
    val heightDp = (info.minHeight.coerceIn(100, 200)).dp
    var showRemove by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clip(RoundedCornerShape(16.dp))
            .background(GnomeColors.Surface.copy(alpha = 0.7f))
            .clickable(onClick = { showRemove = !showRemove })
    ) {
        val widgetHost = remember {
            AppWidgetHost(context, APPWIDGET_HOST_ID).also { it.startListening() }
        }

        DisposableEffect(appWidgetId) {
            onDispose { widgetHost.stopListening() }
        }

        AndroidView(
            factory = {
                widgetHost.createView(context, appWidgetId, info).apply {
                    setAppWidget(appWidgetId, info)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showRemove) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove widget",
                    tint = GnomeColors.Background,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AddWidgetButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GnomeColors.Divider, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = GnomeColors.Accent, modifier = Modifier.size(20.dp))
            Text("Add widget", color = GnomeColors.Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
