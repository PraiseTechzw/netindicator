package com.praisetechzw.netindicator.engine.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import com.praisetechzw.netindicator.engine.NetworkMonitor
import com.praisetechzw.netindicator.engine.NetworkState
import com.praisetechzw.netindicator.engine.NetworkStateDetector
import com.praisetechzw.netindicator.engine.TrafficStatsNetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android Foreground Service holding ongoing lifecycle execution for the core network engine.
 * Isolates real-time calculations from the UI guaranteeing metrics keep evaluating even when the app is closed.
 */
@AndroidEntryPoint
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
    @Inject lateinit var repository: NetworkRepository
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var stateDetector: NetworkStateDetector
    private lateinit var notificationManager: NetPulseNotificationManager

    override fun onCreate() {
        super.onCreate()
        
        notificationManager = NetPulseNotificationManager(applicationContext)
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
        val notification = notificationManager.buildInitialNotification()
        
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

        serviceScope.launch {
            stateDetector.networkStateFlow.collect {
                // Instantly force tracking resets avoiding absurd data deltas during reconnects
                networkMonitor.onNetworkChanged()
            }
        }

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
                Pair(speed, state)
            }.collect { (speed, state) ->
                
                // Track daily usage actively using absolute bytes avoiding double counting abstracts
                if (speed.rxDeltaBytes > 0 || speed.txDeltaBytes > 0) {
                    if (state == NetworkState.WIFI) {
                        repository.addUsage(speed.rxDeltaBytes, speed.txDeltaBytes, 0, 0)
                    } else if (state == NetworkState.MOBILE_DATA) {
                        repository.addUsage(0, 0, speed.rxDeltaBytes, speed.txDeltaBytes)
                    }
                }

                notificationManager.updateNotification(speed, state)
            }
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
