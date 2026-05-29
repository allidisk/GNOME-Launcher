package com.gnome.launcher.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gnome.launcher.ui.GnomeColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GnomeTopBar(
    onActivitiesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }

    // Update clock every minute
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                currentTime = getCurrentTime()
                currentDate = getCurrentDate()
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
        onDispose { context.unregisterReceiver(receiver) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(GnomeColors.TopBar)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Activities button (left)
        Text(
            text = "Activities",
            color = GnomeColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onActivitiesClick() }
        )

        // Center clock
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentTime,
                color = GnomeColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // System tray (right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NetworkWifi,
                contentDescription = "Wi-Fi",
                tint = GnomeColors.TextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = GnomeColors.TextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = GnomeColors.TextPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun getCurrentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun getCurrentDate(): String =
    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
