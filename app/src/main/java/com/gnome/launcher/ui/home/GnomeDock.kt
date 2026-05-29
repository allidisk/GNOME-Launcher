package com.gnome.launcher.ui.home

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gnome.launcher.data.AppInfo
import com.gnome.launcher.ui.GnomeColors
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun GnomeDock(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onAppDrawerClick: () -> Unit,
    onWallpaperClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(GnomeColors.Surface.copy(alpha = 0.88f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.forEach { app ->
                DockIcon(
                    app = app,
                    onClick = { onAppClick(app.packageName) },
                    onLongPress = { onUnpinApp(app.packageName) }
                )
            }

            if (apps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(GnomeColors.Divider)
                )
            }

            // App drawer button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GnomeColors.Accent.copy(alpha = 0.15f))
                    .clickable { onAppDrawerClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "App Drawer",
                    tint = GnomeColors.Accent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun DockIcon(
    app: AppInfo,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon_scale"
    )

    Box(contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(14.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { onClick() },
                        onLongPress = { showMenu = true }
                    )
                }
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = DrawablePainter(app.icon),
                contentDescription = app.label,
                modifier = Modifier.size(44.dp)
            )
        }

        // Long-press context menu: Unpin
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(0.dp, 4.dp),
            containerColor = GnomeColors.Surface,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Unpin from Dock",
                        color = GnomeColors.TextPrimary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    onLongPress()
                }
            )
        }
    }
}
