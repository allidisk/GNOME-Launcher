package com.gnome.launcher.ui.drawer

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
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
fun AppDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAppClick: (String) -> Unit,
    onPinApp: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    isPinned: (String) -> Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GnomeColors.Background.copy(alpha = 0.96f))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 80f) onDismiss()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Activities",
                color = GnomeColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(24.dp))

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(GnomeColors.Surface)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GnomeColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(color = GnomeColors.TextPrimary, fontSize = 15.sp),
                        cursorBrush = SolidColor(GnomeColors.Accent),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("Type to search…", color = GnomeColors.TextDisabled, fontSize = 15.sp)
                            }
                            inner()
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (apps.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No apps found", color = GnomeColors.TextSecondary, fontSize = 16.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppGridItem(
                            app = app,
                            pinned = isPinned(app.packageName),
                            onClick = { onAppClick(app.packageName) },
                            onPin = { onPinApp(app.packageName) },
                            onUnpin = { onUnpinApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppGridItem(
    app: AppInfo,
    pinned: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "app_scale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        Column(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
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
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Image(
                    painter = DrawablePainter(app.icon),
                    contentDescription = app.label,
                    modifier = Modifier.size(56.dp)
                )
                // Pin badge
                if (pinned) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GnomeColors.Accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = GnomeColors.Background,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Text(
                text = app.label,
                color = GnomeColors.TextPrimary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
        }

        // Long-press context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(0.dp, (-8).dp),
            containerColor = GnomeColors.Surface
        ) {
            // App name header
            DropdownMenuItem(
                enabled = false,
                text = {
                    Text(
                        app.label,
                        color = GnomeColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                onClick = {}
            )
            Divider(color = GnomeColors.Divider)

            // Pin / Unpin
            DropdownMenuItem(
                text = {
                    Text(
                        if (pinned) "Unpin from Dock" else "Pin to Dock",
                        color = GnomeColors.TextPrimary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (pinned) MaterialTheme.colorScheme.error else GnomeColors.Accent,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    if (pinned) onUnpin() else onPin()
                }
            )
        }
    }
}
