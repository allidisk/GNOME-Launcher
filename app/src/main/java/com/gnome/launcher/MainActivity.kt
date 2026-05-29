package com.gnome.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.gnome.launcher.ui.GnomeLauncherTheme
import com.gnome.launcher.ui.drawer.AppDrawer
import com.gnome.launcher.ui.home.HomeScreen
import com.gnome.launcher.utils.WallpaperInitializer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set default wallpaper on first launch (no-op on subsequent launches)
        lifecycleScope.launch {
            WallpaperInitializer.setDefaultIfNeeded(this@MainActivity)
        }

        setContent {
            GnomeLauncherTheme {
                GnomeLauncherRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GnomeLauncherRoot(viewModel: LauncherViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.isDrawerOpen) {
        viewModel.closeDrawer()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreen(
            viewModel = viewModel,
            onOpenDrawer = { viewModel.openDrawer() }
        )

        AnimatedVisibility(
            visible = uiState.isDrawerOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = EaseOutCubic)
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = EaseInCubic)
            ) + fadeOut(tween(200))
        ) {
            AppDrawer(
                apps = uiState.filteredApps,
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onAppClick = { viewModel.launchApp(it) },
                onPinApp = { viewModel.pinApp(it) },
                onUnpinApp = { viewModel.unpinApp(it) },
                isPinned = { viewModel.isPinned(it) },
                onDismiss = { viewModel.closeDrawer() }
            )
        }
    }
}
