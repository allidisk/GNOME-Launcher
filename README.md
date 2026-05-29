# GNOME Launcher for Android

A GNOME Shell-inspired Android home screen launcher built with Kotlin + Jetpack Compose.

## Features

- **GNOME Top Bar** — clock, Activities button, system tray icons
- **App Drawer** — swipe up or tap "Activities" to open; animated slide-in overlay
- **Real-time Search** — type to filter all installed apps instantly
- **Auto App Scanning** — reads all launchable apps via `PackageManager`, updates dynamically
- **Widget Support** — hosts Android app widgets on the home screen
- **Wallpaper Support** — tap dock's wallpaper button to change wallpaper
- **Gesture Navigation** — swipe up = open drawer, swipe down = close drawer
- **GNOME Dock** — bottom app dock with your top 5 apps + drawer button

## Color Palette (Catppuccin Mocha — GNOME-inspired)

| Color | Hex | Usage |
|-------|-----|-------|
| Base | `#1E1E2E` | Main background |
| Surface | `#313244` | Cards, panels |
| Accent | `#89B4FA` | Interactive elements |
| Text | `#CDD6F4` | Primary text |

## Project Structure

```
app/src/main/java/com/gnome/launcher/
├── MainActivity.kt               # Root activity + animated nav
├── LauncherViewModel.kt          # App state, search, drawer
├── data/
│   ├── AppInfo.kt                # App data model
│   └── AppRepository.kt         # PackageManager scanning
└── ui/
    ├── GnomeTheme.kt             # Colors + MaterialTheme
    ├── home/
    │   ├── HomeScreen.kt         # Main home with wallpaper + gestures
    │   ├── GnomeTopBar.kt        # GNOME Shell top bar
    │   ├── GnomeDock.kt          # Bottom app dock
    │   └── WallpaperPickerActivity.kt
    ├── drawer/
    │   └── AppDrawer.kt          # Activities overlay + search + grid
    └── widgets/
        ├── WidgetArea.kt         # Widget host area
        └── WidgetPickerActivity.kt
```

## Setup Instructions

1. **Open in Android Studio** — File > Open > select the `gnome-launcher` folder
2. **Add missing dependency** — in `app/build.gradle`, Coil's `DrawablePainter` needs:
   ```groovy
   implementation 'com.google.accompanist:accompanist-drawablepainter:0.32.0'
   ```
3. **Sync Gradle** — Click "Sync Now"
4. **Run on device or emulator** (API 26+)
5. **Set as default launcher** — When prompted "How do you want to open Home?", select **GNOME Launcher** and tap **Always**

## Customizing the Dock

In `HomeScreen.kt`, change `take(5)` to show more/fewer pinned apps:
```kotlin
apps = uiState.allApps.take(5)  // Show first 5 apps alphabetically
```

For a truly pinned dock (specific apps), store packageNames in a preferences list
and filter `allApps` by those packageNames in the ViewModel.

## Adding Widgets

Long-press support and a full widget picker flow can be added by:
1. Calling `AppWidgetHost.allocateAppWidgetId()`
2. Launching `WidgetPickerActivity` with the new ID
3. Storing the widget ID in `DataStore` or `SharedPreferences`
4. Passing IDs to `WidgetArea` to render them

## Permissions Required

| Permission | Purpose |
|------------|---------|
| `SET_WALLPAPER` | Change the system wallpaper |
| `BIND_APPWIDGET` | Host app widgets |
| `READ_EXTERNAL_STORAGE` | Pick wallpaper from gallery |
