package com.praisetechzw.netindicator.utils

import kotlin.math.log10
import kotlin.math.pow

object FormatUtils {

    /**
     * Formats bytes-per-second into a human-readable speed string.
     */
    fun formatSpeed(bps: Long?, unit: String = "AUTO"): String {
        if (bps == null || bps < 0) return "–"
        return when {
            unit == "KBPS" -> "%.1f KB/s".format(bps / 1_024.0)
            unit == "MBPS" -> "%.2f MB/s".format(bps / (1_024.0 * 1_024.0))
            bps < 1_024 -> "$bps B/s"
            bps < 1_048_576 -> "%.1f KB/s".format(bps / 1_024.0)
            bps < 1_073_741_824 -> "%.2f MB/s".format(bps / 1_048_576.0)
            else -> "%.2f GB/s".format(bps / 1_073_741_824.0)
        }
    }

    /**
     * Formats a byte count into a compact human-readable string.
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return "%.1f %s".format(bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }

    /**
     * Formats ping in milliseconds with qualifier.
     */
    fun formatPing(ms: Long?): String {
        if (ms == null) return "–"
        return "${ms}ms"
    }

    /**
     * Returns a CSS-inspired quality label for ping.
     */
    fun pingQuality(ms: Long?): String = when {
        ms == null -> "Unknown"
        ms < 20 -> "Excellent"
        ms < 60 -> "Good"
        ms < 120 -> "Fair"
        ms < 250 -> "Poor"
        else -> "Bad"
    }

    /**
     * Returns a signal strength label for dBm.
     */
    fun signalQuality(dbm: Int?): String = when {
        dbm == null -> "Unknown"
        dbm >= -55 -> "Excellent"
        dbm >= -65 -> "Good"
        dbm >= -75 -> "Fair"
        dbm >= -85 -> "Poor"
        else -> "Very Poor"
    }
}
