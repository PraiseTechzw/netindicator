package com.praisetechzw.netindicator.domain.model

/**
 * Connection type classification.
 */
enum class NetworkType {
    WIFI, MOBILE_4G, MOBILE_5G, MOBILE_3G, MOBILE_2G, ETHERNET, NONE, UNKNOWN
}

/**
 * Domain model representing a real-time network snapshot.
 */
data class NetworkSnapshot(
    val timestamp: Long,
    val networkType: NetworkType,
    val ssid: String?,
    val signalStrength: Int?,          // dBm
    val downloadSpeedBps: Long?,
    val uploadSpeedBps: Long?,
    val pingMs: Long?,
    val isConnected: Boolean,
    val ipAddress: String?,
    val networkOperator: String?,
    val linkSpeedMbps: Int?
)

/**
 * Domain model for a historical network event displayed in the UI.
 */
data class NetworkEvent(
    val id: Long,
    val timestamp: Long,
    val networkType: NetworkType,
    val ssid: String?,
    val signalStrength: Int?,
    val downloadSpeedBps: Long?,
    val uploadSpeedBps: Long?,
    val pingMs: Long?,
    val isConnected: Boolean,
    val ipAddress: String?,
    val networkOperator: String?,
    val linkSpeedMbps: Int?
)

/**
 * Aggregated daily usage stats tracking precise boundaries across network interfaces.
 */
data class DailyUsageSummary(
    val date: String,
    val totalDownloadBytes: Long,
    val totalUploadBytes: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val avgPingMs: Long,
    val avgDownloadBps: Long,
    val avgUploadBps: Long,
    val sessionCount: Int,
    val wifiDurationMs: Long,
    val mobileDurationMs: Long
)

/**
 * Aggregated stats for the Stats screen.
 */
data class NetworkStats(
    val avgDownloadBps: Double,
    val avgUploadBps: Double,
    val avgPingMs: Double,
    val totalEvents: Int,
    val connectedEvents: Int,
    val uptimePercent: Double
)
