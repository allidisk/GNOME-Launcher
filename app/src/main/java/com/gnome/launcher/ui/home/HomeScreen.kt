package com.gnome.launcher.ui.home

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gnome.launcher.LauncherViewModel
import com.gnome.launcher.data.AppInfo
import com.gnome.launcher.data.HomeItem
import com.gnome.launcher.ui.GnomeColors
import com.gnome.launcher.ui.widgets.WidgetPickerActivity
import com.google.accompanist.drawablepainter.DrawablePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { uiState.pages.size })

    LaunchedEffect(pagerState.currentPage) { viewModel.setCurrentPage(pagerState.currentPage) }

    Box(
        modifier = modifier.fillMaxSize().background(GnomeColors.Background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, d -> if (d < -80f) onOpenDrawer() }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, GnomeColors.Background.copy(alpha = 0.2f)))
        ))

        Column(modifier = Modifier.fillMaxSize()) {
            GnomeTopBar(onActivitiesClick = onOpenDrawer)
            Row(modifier = Modifier.weight(1f)) {
                GnomeLeftDock(
                    apps = uiState.dockApps,
                    onAppClick = { viewModel.launchApp(it) },
                    onUnpinApp = { viewModel.unpinFromDock(it) },
                    onDrawerClick = onOpenDrawer
                )
                Column(modifier = Modifier.weight(1f)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
                        HomePage(
                            items = uiState.pages.getOrElse(pageIndex) { emptyList() },
                            onItemClick = { item ->
                                when (item) {
                                    is HomeItem.App -> viewModel.launchApp(item.info.packageName)
                                    is HomeItem.Folder -> viewModel.openFolder(item)
                                }
                            },
                            onRemoveItem = { viewModel.removeFromPage(it, pageIndex) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(uiState.pages.size) { i ->
                            Box(modifier = Modifier.padding(3.dp)
                                .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == pagerState.currentPage) GnomeColors.Accent else GnomeColors.TextDisabled)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Add, "Add page", tint = GnomeColors.TextDisabled,
                            modifier = Modifier.size(18.dp).clickable { viewModel.addPage() })
                    }
                }
            }
        }

        uiState.openFolder?.let { folder ->
            FolderDialog(
                folder = folder,
                onDismiss = { viewModel.closeFolder() },
                onLaunch = { viewModel.launchApp(it) },
                onRemoveApp = { viewModel.removeAppFromFolder(folder.id, it) },
                onRename = { viewModel.renameFolder(folder.id, it) }
            )
        }
    }
}

@Composable
fun GnomeLeftDock(
    apps: List<AppInfo>,
    onAppClick: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onDrawerClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight().padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GnomeColors.Surface.copy(alpha = 0.85f))
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape)
            .background(GnomeColors.Accent.copy(alpha = 0.15f)).clickable { onDrawerClick() },
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Apps, "App Drawer", tint = GnomeColors.Accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.width(32.dp).height(1.dp).background(GnomeColors.Divider))
        apps.forEach { app ->
            var showMenu by remember { mutableStateOf(false) }
            Box(contentAlignment = Alignment.Center) {
                Image(DrawablePainter(app.icon), app.label,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onAppClick(app.packageName) }, onLongPress = { showMenu = true })
                        })
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false },
                    offset = DpOffset(48.dp, 0.dp)) {
                    DropdownMenuItem(
                        text = { Text("Unpin from Dock", color = GnomeColors.TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onUnpinApp(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomePage(items: List<HomeItem>, onItemClick: (HomeItem) -> Unit, onRemoveItem: (HomeItem) -> Unit) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Swipe up for apps", color = GnomeColors.TextDisabled, fontSize = 13.sp)
                Text("Long-press apps to add here", color = GnomeColors.TextDisabled, fontSize = 13.sp)
            }
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    var showMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onItemClick(item) }, onLongPress = { showMenu = true })
                                }.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            when (item) {
                                is HomeItem.App -> {
                                    Image(DrawablePainter(item.info.icon), item.info.label, modifier = Modifier.size(52.dp))
                                    Text(item.info.label, color = GnomeColors.TextPrimary, fontSize = 11.sp,
                                        textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                is HomeItem.Folder -> {
                                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                                        .background(GnomeColors.SurfaceVariant), contentAlignment = Alignment.Center) {
                                        val preview = item.apps.take(4)
                                        if (preview.size >= 4) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    preview.take(2).forEach { Image(DrawablePainter(it.icon), null, modifier = Modifier.size(22.dp)) }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    preview.drop(2).forEach { Image(DrawablePainter(it.icon), null, modifier = Modifier.size(22.dp)) }
                                                }
                                            }
                                        } else preview.firstOrNull()?.let { Image(DrawablePainter(it.icon), null, modifier = Modifier.size(36.dp)) }
                                    }
                                    Text(item.name, color = GnomeColors.TextPrimary, fontSize = 11.sp,
                                        textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Remove from page", color = GnomeColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onRemoveItem(item) }
                            )
                        }
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun FolderDialog(folder: HomeItem.Folder, onDismiss: () -> Unit, onLaunch: (String) -> Unit,
    onRemoveApp: (String) -> Unit, onRename: (String) -> Unit) {
    var editingName by remember { mutableStateOf(false) }
    var nameText by remember(folder.name) { mutableStateOf(folder.name) }
    Dialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(GnomeColors.Surface).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editingName) {
                    OutlinedTextField(value = nameText, onValueChange = { nameText = it },
                        modifier = Modifier.weight(1f), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GnomeColors.TextPrimary, unfocusedTextColor = GnomeColors.TextPrimary,
                            focusedBorderColor = GnomeColors.Accent, unfocusedBorderColor = GnomeColors.Divider))
                    IconButton(onClick = { onRename(nameText); editingName = false }) {
                        Icon(Icons.Default.Check, null, tint = GnomeColors.Accent)
                    }
                } else {
                    Text(folder.name, color = GnomeColors.TextPrimary, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { editingName = true }) {
                        Icon(Icons.Default.Edit, null, tint = GnomeColors.TextSecondary)
                    }
                }
            }
            folder.apps.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { app ->
                        Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { onLaunch(app.packageName) }, onLongPress = { onRemoveApp(app.packageName) })
                            }.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Image(DrawablePainter(app.icon), app.label, modifier = Modifier.size(44.dp))
                            Text(app.label, color = GnomeColors.TextPrimary, fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
