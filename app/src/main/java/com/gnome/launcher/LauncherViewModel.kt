package com.gnome.launcher

import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnome.launcher.data.AppInfo
import com.gnome.launcher.data.AppRepository
import com.gnome.launcher.data.PreferencesRepository
import com.gnome.launcher.ui.widgets.APPWIDGET_HOST_ID
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LauncherUiState(
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val pinnedApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isDrawerOpen: Boolean = false,
    val isLoading: Boolean = true,
    val widgetIds: List<Int> = emptyList(),
    // Long-press context menu
    val contextMenuApp: AppInfo? = null,
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val prefs = PreferencesRepository(application)

    val widgetHost = AppWidgetHost(application, APPWIDGET_HOST_ID)
    val widgetManager: AppWidgetManager = AppWidgetManager.getInstance(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        widgetHost.startListening()
        loadApps()
        observePinnedApps()
        observeWidgets()
    }

    override fun onCleared() {
        super.onCleared()
        widgetHost.stopListening()
    }

    fun loadApps() {
        viewModelScope.launch {
            repository.getInstalledApps().collect { apps ->
                _uiState.update { state ->
                    val pinned = buildPinnedList(apps, state.pinnedApps.map { it.packageName })
                    state.copy(allApps = apps, filteredApps = apps, pinnedApps = pinned, isLoading = false)
                }
            }
        }
    }

    private fun observePinnedApps() {
        viewModelScope.launch {
            prefs.pinnedAppPackages.collect { packages ->
                _uiState.update { state ->
                    state.copy(pinnedApps = buildPinnedList(state.allApps, packages))
                }
            }
        }
    }

    private fun observeWidgets() {
        viewModelScope.launch {
            prefs.widgetIds.collect { ids ->
                _uiState.update { it.copy(widgetIds = ids) }
            }
        }
    }

    private fun buildPinnedList(all: List<AppInfo>, packages: List<String>): List<AppInfo> {
        val map = all.associateBy { it.packageName }
        return packages.mapNotNull { map[it] }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) state.allApps
            else state.allApps.filter { it.label.contains(query, ignoreCase = true) }
            state.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    // ── Drawer ───────────────────────────────────────────────────────────────

    fun openDrawer() { _uiState.update { it.copy(isDrawerOpen = true) } }

    fun closeDrawer() {
        _uiState.update {
            it.copy(isDrawerOpen = false, searchQuery = "", filteredApps = it.allApps, contextMenuApp = null)
        }
    }

    fun launchApp(packageName: String) {
        repository.launchApp(packageName)
        closeDrawer()
    }

    // ── Context menu ─────────────────────────────────────────────────────────

    fun showContextMenu(app: AppInfo) { _uiState.update { it.copy(contextMenuApp = app) } }
    fun dismissContextMenu() { _uiState.update { it.copy(contextMenuApp = null) } }

    fun pinApp(packageName: String) {
        viewModelScope.launch { prefs.pinApp(packageName) }
        dismissContextMenu()
    }

    fun unpinApp(packageName: String) {
        viewModelScope.launch { prefs.unpinApp(packageName) }
        dismissContextMenu()
    }

    fun isPinned(packageName: String): Boolean =
        _uiState.value.pinnedApps.any { it.packageName == packageName }

    // ── Widgets ──────────────────────────────────────────────────────────────

    fun addWidget(appWidgetId: Int) {
        viewModelScope.launch { prefs.addWidget(appWidgetId) }
    }

    fun removeWidget(appWidgetId: Int) {
        widgetHost.deleteAppWidgetId(appWidgetId)
        viewModelScope.launch { prefs.removeWidget(appWidgetId) }
    }

    fun allocateWidgetId(): Int = widgetHost.allocateAppWidgetId()
}
