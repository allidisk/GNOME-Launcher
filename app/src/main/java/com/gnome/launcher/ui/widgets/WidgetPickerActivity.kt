package com.gnome.launcher.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Manages the full widget-add flow:
 *   1. Allocate a new widget ID
 *   2. Launch system widget picker (ACTION_APPWIDGET_PICK)
 *   3. If the widget needs configuration, launch its config activity
 *   4. Return the final widget ID to the caller via RESULT_OK
 */
class WidgetPickerActivity : ComponentActivity() {

    private lateinit var widgetHost: AppWidgetHost
    private var pendingWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    // Step 3: handle config activity result
    private val configLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            deliverResult(pendingWidgetId)
        } else {
            // User cancelled config — release the ID
            widgetHost.deleteAppWidgetId(pendingWidgetId)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Step 2: handle picker result
    private val pickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val widgetId = result.data!!.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                cancelAndFinish()
                return@registerForActivityResult
            }
            pendingWidgetId = widgetId
            launchConfigIfNeeded(widgetId)
        } else {
            cancelAndFinish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)

        // Step 1: allocate ID and launch picker
        val newWidgetId = widgetHost.allocateAppWidgetId()

        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newWidgetId)
        }
        pickerLauncher.launch(pickIntent)
    }

    private fun launchConfigIfNeeded(widgetId: Int) {
        val widgetInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)

        if (widgetInfo?.configure != null) {
            // Widget requires a config screen
            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = widgetInfo.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            configLauncher.launch(configIntent)
        } else {
            // No config needed — done
            deliverResult(widgetId)
        }
    }

    private fun deliverResult(widgetId: Int) {
        val data = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun cancelAndFinish() {
        if (pendingWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            widgetHost.deleteAppWidgetId(pendingWidgetId)
        }
        setResult(RESULT_CANCELED)
        finish()
    }
}
