package com.gnome.launcher.utils

import android.app.WallpaperManager
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.gnome.launcher.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.wallpaperDataStore by preferencesDataStore("wallpaper_prefs")
private val WALLPAPER_SET_KEY = booleanPreferencesKey("default_wallpaper_set")

object WallpaperInitializer {

    /**
     * Sets the default wallpaper exactly once (on first ever launch).
     * Subsequent launches skip this entirely.
     */
    suspend fun setDefaultIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.wallpaperDataStore.data.first()
        if (prefs[WALLPAPER_SET_KEY] == true) return@withContext

        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            context.resources.openRawResource(R.drawable.default_wallpaper).use { stream ->
                wallpaperManager.setStream(stream)
            }
            context.wallpaperDataStore.edit { it[WALLPAPER_SET_KEY] = true }
        } catch (e: Exception) {
            // Silently fail — user can always set wallpaper manually
            e.printStackTrace()
        }
    }
}
