package com.praisetechzw.netindicator.engine

/**
 * Data class holding real-time calculated network speed.
 */
data class NetworkSpeed(
    val downloadBytesPerSec: Long,
    val uploadBytesPerSec: Long,
    val totalBytesPerSec: Long,
    val rxDeltaBytes: Long = 0,
    val txDeltaBytes: Long = 0
)

/**
 * Interface defining the contract for the core network speed monitor engine.
 */
interface NetworkMonitor {
    
    /**
     * Connects to the system APIs to begin monitoring.
     * Starts emitting data via [speedFlow].
     */
    fun startMonitoring()

    /**
     * Halts calculation and disconnects.
     */
    fun stopMonitoring()
    
    /**
     * Signals a network transport change, forcing the monitor to reset internally
     * and avoid extreme byte deltas or crashes when OS hardware pointers drop.
     */
    fun onNetworkChanged()
    
    /**
     * Flow of calculated network speeds, typically emitting once per second.
     */
    val speedFlow: kotlinx.coroutines.flow.StateFlow<NetworkSpeed>
}
