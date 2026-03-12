package com.praisetechzw.netindicator.engine.service

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
import com.praisetechzw.netindicator.engine.NetworkMonitor
import com.praisetechzw.netindicator.engine.NetworkStateDetector
import com.praisetechzw.netindicator.engine.SpeedFormatter
import com.praisetechzw.netindicator.engine.TrafficStatsNetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Android Foreground Service holding ongoing lifecycle execution for the core network engine.
 * Isolates real-time calculations from the UI guaranteeing metrics keep evaluating even when the app is closed.
 */
class NetPulseService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START_MONITORING"
        const val ACTION_STOP = "ACTION_STOP_MONITORING"
        const val NOTIFICATION_ID = 48291
        const val CHANNEL_ID = "net_pulse_foreground_channel"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var uiUpdateJob: Job? = null

    // Component instances
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var stateDetector: NetworkStateDetector

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Init the engine components
        networkMonitor = TrafficStatsNetworkMonitor(serviceScope)
        stateDetector = NetworkStateDetector(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundMonitoring()
            ACTION_STOP -> stopForegroundMonitoring()
        }
        return START_STICKY // System will restart it if killed via memory pressure
    }

    private fun startForegroundMonitoring() {
        // Enforce foreground constraints within 5 seconds as Android system demands
        val notification = buildNotification("Initializing monitor…", "Waiting for speed tracking")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        ServiceController.updateServiceState(true)

        // Spin up the core engines
        stateDetector.startDetection()
        networkMonitor.startMonitoring()

        updateDynamicNotification()
    }

    private fun stopForegroundMonitoring() {
        // Safely kill jobs and release network listeners immediately
        uiUpdateJob?.cancel()
        stateDetector.stopDetection()
        networkMonitor.stopMonitoring()
        
        ServiceController.updateServiceState(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf() // Commits complete shutdown
    }

    /**
     * Subscribes to the live flows generated from the mathematical engine and actively reconstructs 
     * the persistent notification to show the user real-time bandwidth usage dynamically.
     */
    private fun updateDynamicNotification() {
        uiUpdateJob?.cancel()
        uiUpdateJob = serviceScope.launch {
            // Combine limits the UI refresh to trigger predictably matching engine output states
            combine(networkMonitor.speedFlow, stateDetector.networkStateFlow) { speed, state ->
                val title = "NetPulse • ${state.name.replace("_", " ")}"
                val speedContent = buildString {
                    append("↓ ${SpeedFormatter.formatSpeed(speed.downloadBytesPerSec)}  ")
                    append("↑ ${SpeedFormatter.formatSpeed(speed.uploadBytesPerSec)}")
                }
                
                buildNotification(title, speedContent)
            }.collect { notification ->
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingLaunch = PendingIntent.getActivity(
            this, 0, launchIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_network_monitor)
            .setContentIntent(pendingLaunch)
            // Low priority prevents aggressive user-facing pinging sounds every second the speed changes
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) 
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NetPulse Core Monitor",
                NotificationManager.IMPORTANCE_LOW 
            ).apply {
                description = "Shows continuous internet speeds dynamically on the status bar"
                setShowBadge(false)
            }
            
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevents hard memory leaks when Android violently destroys the service.
        serviceScope.cancel() 
        ServiceController.updateServiceState(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
