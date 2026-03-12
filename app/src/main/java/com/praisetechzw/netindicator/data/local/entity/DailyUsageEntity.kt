package com.praisetechzw.netindicator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a daily network usage summary record.
 */
@Entity(tableName = "daily_usage")
data class DailyUsageEntity(
    @PrimaryKey
    val dateEpochDay: Long,              // epoch day (millis / 86_400_000)
    val date: String,                    // "yyyy-MM-dd" for display
    val totalDownloadBytes: Long = 0,
    val totalUploadBytes: Long = 0,
    val avgPingMs: Long = 0,
    val avgDownloadBps: Long = 0,
    val avgUploadBps: Long = 0,
    val sessionCount: Int = 0,
    val wifiDurationMs: Long = 0,
    val mobileDurationMs: Long = 0
)
