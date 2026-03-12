package com.praisetechzw.netindicator.engine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.praisetechzw.netindicator.MainActivity
import com.praisetechzw.netindicator.R
import com.praisetechzw.netindicator.engine.NetworkSpeed
import com.praisetechzw.netindicator.engine.NetworkState
import com.praisetechzw.netindicator.engine.SpeedFormatter

/**
 * Defines what speed metrics are shown in the persistent notification.
 */
enum class NotificationDisplayMode {
    TOTAL_SPEED,
    UPLOAD_ONLY,
    DOWNLOAD_ONLY,
    UPLOAD_AND_DOWNLOAD
}

/**
 * Dedicated manager class for constructing and updating the foreground service notification.
 * Decouples rendering logic from the core tracking engine.
 */
class NetPulseNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

// Configurable tracking settings (could be wired to DataStore later)
var displayMode: NotificationDisplayMode = NotificationDisplayMode.UPLOAD_AND_DOWNLOAD
var hideWhenDisconnected: Boolean = false

private val pendingIntent: PendingIntent by lazy {
    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    PendingIntent.getActivity(
        context, 0, launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

init {
    createChannel()
}

/**
 * Creates the initial placeholder notification required by Android when starting
 * a Foreground Service before the first speed metric is calculated.
 */
fun buildInitialNotification(): Notification {
    return buildBaseNotification("Initializing Monitor…", "Waiting for network data")
}

/**
 * Main update function called dynamically by the service flow.
 * Formats the incoming throughput into the compact display mode.
 */
fun updateNotification(speed: NetworkSpeed, state: NetworkState) {
    if (state == NetworkState.DISCONNECTED && hideWhenDisconnected) {
        // In a true foreground service, you CANNOT completely hide the notification.
        // The best approach is to show a minimal, silent disconnected state.
        val notif = buildBaseNotification("NetPulse", "Disconnected")
        notificationManager.notify(NetPulseService.NOTIFICATION_ID, notif)
        return
    }

    val title = formatTitle(state, speed)
    val content = formatContent(speed, state)

    val notification = buildBaseNotification(title, content)
    notificationManager.notify(NetPulseService.NOTIFICATION_ID, notification)
}

private fun formatTitle(state: NetworkState, speed: NetworkSpeed): String {
    val prefix = when (state) {
        NetworkState.DISCONNECTED -> "Disconnected"
        NetworkState.UNKNOWN -> "Connecting…"
        else -> state.name.replace("_", " ")
    }
    
    // If we only want to show TOTAL speed, returning it in the title saves space
    return if (displayMode == NotificationDisplayMode.TOTAL_SPEED && state != NetworkState.DISCONNECTED) {
        "$prefix • ${SpeedFormatter.formatSpeed(speed.totalBytesPerSec)}"
    } else {
        prefix
    }
}

private fun formatContent(speed: NetworkSpeed, state: NetworkState): String {
    if (state == NetworkState.DISCONNECTED) {
        return "No active internet connection"
    }

    return when (displayMode) {
        NotificationDisplayMode.TOTAL_SPEED -> {
            "Total bandwidth: ${SpeedFormatter.formatSpeed(speed.totalBytesPerSec)}"
        }
        NotificationDisplayMode.DOWNLOAD_ONLY -> {
            "↓ ${SpeedFormatter.formatSpeed(speed.downloadBytesPerSec)}"
        }
        NotificationDisplayMode.UPLOAD_ONLY -> {
            "↑ ${SpeedFormatter.formatSpeed(speed.uploadBytesPerSec)}"
        }
        NotificationDisplayMode.UPLOAD_AND_DOWNLOAD -> {
            "↓ ${SpeedFormatter.formatSpeed(speed.downloadBytesPerSec)}  ↑ ${SpeedFormatter.formatSpeed(speed.uploadBytesPerSec)}"
        }
    }
}

/**
 * Constructs the core immutable properties of the notification preventing boilerplate.
 */
private fun buildBaseNotification(title: String, content: String): Notification {
    return NotificationCompat.Builder(context, NetPulseService.CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_network_monitor)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_LOW) // Important: Prevents sound/vibration spam
        .setOngoing(true)
        .setOnlyAlertOnce(true) // Crucial for frequent updates
        .build()
}

private fun createChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NetPulseService.CHANNEL_ID,
            "NetPulse Speed Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Displays real-time internet speeds in the status bar"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
}
