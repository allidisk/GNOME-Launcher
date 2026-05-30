package com.gnome.launcher.ui.home

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class WallpaperPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(WallpaperManager.ACTION_SET_WALLPAPER)
        startActivity(Intent.createChooser(intent, "Select Wallpaper"))
        finish()
    }
}
