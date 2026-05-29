package com.gnome.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class PreferencesRepository(private val context: Context) {

    companion object {
        private val PINNED_APPS_KEY = stringPreferencesKey("pinned_apps")
        private val WIDGET_IDS_KEY = stringPreferencesKey("widget_ids")
        private const val SEPARATOR = ","
    }

    // ── Pinned Apps ──────────────────────────────────────────────────────────

    val pinnedAppPackages: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[PINNED_APPS_KEY]
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    suspend fun pinApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PINNED_APPS_KEY]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?.toMutableList()
                ?: mutableListOf()
            if (!current.contains(packageName)) {
                current.add(packageName)
            }
            prefs[PINNED_APPS_KEY] = current.joinToString(SEPARATOR)
        }
    }

    suspend fun unpinApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PINNED_APPS_KEY]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?.toMutableList()
                ?: mutableListOf()
            current.remove(packageName)
            prefs[PINNED_APPS_KEY] = current.joinToString(SEPARATOR)
        }
    }

    // ── Widget IDs ───────────────────────────────────────────────────────────

    val widgetIds: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        prefs[WIDGET_IDS_KEY]
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toIntOrNull() }
            ?: emptyList()
    }

    suspend fun addWidget(appWidgetId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[WIDGET_IDS_KEY]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?.toMutableList()
                ?: mutableListOf()
            if (!current.contains(appWidgetId.toString())) {
                current.add(appWidgetId.toString())
            }
            prefs[WIDGET_IDS_KEY] = current.joinToString(SEPARATOR)
        }
    }

    suspend fun removeWidget(appWidgetId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[WIDGET_IDS_KEY]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?.toMutableList()
                ?: mutableListOf()
            current.remove(appWidgetId.toString())
            prefs[WIDGET_IDS_KEY] = current.joinToString(SEPARATOR)
        }
    }
}
