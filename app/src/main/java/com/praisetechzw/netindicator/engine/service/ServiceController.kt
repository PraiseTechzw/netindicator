package com.praisetechzw.netindicator.engine.service

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller class that ViewModels and activities can use to systematically interact
 * with the Foreground Service without directly managing Intent structures.
 */
class ServiceController(private val context: Context) {

    companion object {
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        // Internal setter for the service to bind its own lifecycle
        internal fun updateServiceState(running: Boolean) {
            _isRunning.value = running
        }
    }

    /**
     * Starts the persistent foreground monitor.
     */
    fun startMonitoring() {
        if (_isRunning.value) return // Prevent duplicate intense starts

        val intent = Intent(context, NetPulseService::class.java).apply {
            action = NetPulseService.ACTION_START
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Halts all tracking and shuts down the service cleanly avoiding memory leaks.
     */
    fun stopMonitoring() {
        if (!_isRunning.value) return 

        val intent = Intent(context, NetPulseService::class.java).apply {
            action = NetPulseService.ACTION_STOP
        }
        context.startService(intent)
    }
}
