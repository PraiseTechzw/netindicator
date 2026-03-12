package com.praisetechzw.netindicator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single network event snapshot stored in the local database.
 */
@Entity(tableName = "network_events")
data class NetworkEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,                  // epoch millis
    val networkType: String,              // WIFI, MOBILE, NONE, etc.
    val ssid: String?,                    // Wi-Fi SSID (null for mobile/none)
    val signalStrength: Int?,             // dBm or RSSI level
    val downloadSpeedBps: Long?,          // bytes per second
    val uploadSpeedBps: Long?,            // bytes per second
    val pingMs: Long?,                    // round-trip latency in ms
    val isConnected: Boolean,
    val ipAddress: String?,
    val networkOperator: String?,         // carrier name for mobile
    val linkSpeedMbps: Int?               // Wi-Fi link speed
)
