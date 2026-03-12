package com.praisetechzw.netindicator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.praisetechzw.netindicator.MainActivity
import com.praisetechzw.netindicator.R
import com.praisetechzw.netindicator.data.preferences.AppPreferencesDataSource
import com.praisetechzw.netindicator.domain.model.NetworkSnapshot
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import com.praisetechzw.netindicator.utils.FormatUtils
import com.praisetechzw.netindicator.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkMonitorService : Service() {

    @Inject lateinit var repository: NetworkRepository
    @Inject lateinit var preferences: AppPreferencesDataSource

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "net_monitor_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        private val _currentSnapshot = MutableStateFlow<NetworkSnapshot?>(null)
        val currentSnapshot: StateFlow<NetworkSnapshot?> = _currentSnapshot.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> startMonitoring()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun startMonitoring() {
        // Promote to foreground immediately to satisfy Android foreground service requirements
        val notification = buildNotification("Initializing…", "Starting network monitor")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            while (true) {
                val settings = preferences.settingsFlow.first()
                val snapshot = NetworkUtils.buildSnapshot(applicationContext)
                _currentSnapshot.value = snapshot

                // Prune old data periodically before saving
                repository.pruneOldData(settings.retentionDays)
                repository.saveSnapshot(snapshot)

                if (settings.showNotifications) {
                    updateNotification(snapshot)
                }

                delay(settings.pollingIntervalSeconds * 1_000L)
            }
        }
    }

    private fun updateNotification(snapshot: NetworkSnapshot) {
        val title = if (snapshot.isConnected) {
            "${snapshot.networkType.name} • ${snapshot.ssid ?: snapshot.networkOperator ?: "Connected"}"
        } else {
            "No Connection"
        }
        val body = buildString {
            snapshot.downloadSpeedBps?.let { append("↓ ${FormatUtils.formatSpeed(it)}  ") }
            snapshot.uploadSpeedBps?.let { append("↑ ${FormatUtils.formatSpeed(it)}  ") }
            snapshot.pingMs?.let { append("Ping ${FormatUtils.formatPing(it)}") }
            if (isEmpty()) append("Monitoring active")
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(title, body))
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_network_monitor)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent network monitoring status"
                setShowBadge(false)
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
