package com.praisetechzw.netindicator.engine

import android.net.TrafficStats
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Core engine implementation that calculates network speeds accurately using [TrafficStats].
 * Runs entirely off the main thread to ensure efficient CPU usage and avoids UI lagging.
 */
class TrafficStatsNetworkMonitor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val updateIntervalMs: Long = 1000L
) : NetworkMonitor {

    private val _speedFlow = MutableStateFlow(NetworkSpeed(0, 0, 0))
    override val speedFlow: StateFlow<NetworkSpeed> = _speedFlow.asStateFlow()

    private var monitorJob: Job? = null

    // State holding variables for delta calculation
    private var lastRxBytes: Long = -1L
    private var lastTxBytes: Long = -1L
    private var lastTimeMs: Long = -1L

    override fun startMonitoring() {
        if (monitorJob?.isActive == true) return

        // Initialize baseline values on start
        resetBaselines()

        monitorJob = scope.launch {
            while (isActive) {
                delay(updateIntervalMs)
                calculateSpeeds()
            }
        }
    }

    override fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        resetBaselines()
        _speedFlow.value = NetworkSpeed(0, 0, 0)
    }

    /**
     * Resets internal tracking. This handles first measurement samples, network switching
     * issues, or sudden disconnections neatly without emitting absurdly huge deltas.
     */
    fun onNetworkChanged() {
        resetBaselines()
    }

    private fun resetBaselines() {
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTimeMs = SystemClock.elapsedRealtime()
    }

    private fun calculateSpeeds() {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTimeMs = SystemClock.elapsedRealtime()

        // Handle edge case: no traffic tracked by OS at all yet
        if (currentRx == TrafficStats.UNSUPPORTED.toLong() || 
            currentTx == TrafficStats.UNSUPPORTED.toLong()) {
            _speedFlow.value = NetworkSpeed(0, 0, 0)
            return
        }

        // Handle edge case: very first sample before delta initialization
        if (lastRxBytes == -1L || lastTxBytes == -1L || lastTimeMs == -1L) {
            resetBaselines()
            return
        }

        val timeDeltaMs = currentTimeMs - lastTimeMs
        
        // Prevent Divide by Zero just in case
        if (timeDeltaMs <= 0) return

        var rxDelta = currentRx - lastRxBytes
        var txDelta = currentTx - lastTxBytes

        // Handle edge cases: Negative deltas or sudden resets natively happen
        // when device reboots, network stack resets, or switches connections
        if (rxDelta < 0 || txDelta < 0) {
            rxDelta = 0
            txDelta = 0
            resetBaselines() // Fix corrupt tracking by forcing a new baseline calculation
        } else {
            // Only update history if diff makes sense
            lastRxBytes = currentRx
            lastTxBytes = currentTx
            lastTimeMs = currentTimeMs
        }

        // Bytes normalized per second accurately tracking actual elapsed ms over 1000
        val downloadBps = (rxDelta * 1000) / timeDeltaMs
        val uploadBps = (txDelta * 1000) / timeDeltaMs

        _speedFlow.value = NetworkSpeed(
            downloadBytesPerSec = downloadBps,
            uploadBytesPerSec = uploadBps,
            totalBytesPerSec = downloadBps + uploadBps,
            rxDeltaBytes = rxDelta,
            txDeltaBytes = txDelta
        )
    }
}
