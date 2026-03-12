package com.praisetechzw.netindicator.engine

import kotlin.math.log10
import kotlin.math.pow

/**
 * Dedicated utility handling real-time speed metric conversions for the Core Engine. 
 * Formats bandwidth accurately into KB/s, MB/s, or optionally bit-orientated metrics like Mbps.
 */
object SpeedFormatter {

    /**
     * Enum controlling formatting preferences
     */
    enum class FormattingMode {
        AUTO_BYTES,     // Auto scaling bytes (B/s, KB/s, MB/s, GB/s)
        AUTO_BITS,      // Auto scaling bits (bps, Kbps, Mbps, Gbps)
        FORCE_KBS,      // Forces KB/s mathematically
        FORCE_MBS       // Forces MB/s mathematically
    }

    /**
     * Public formatting abstraction mapping long byte/sec to string metric types.
     * Use [mode] to enforce optional bits/sec over bytes/sec.
     */
    fun formatSpeed(
        bytesPerSec: Long?,
        mode: FormattingMode = FormattingMode.AUTO_BYTES
    ): String {
        // Fallback or un-initalized
        if (bytesPerSec == null || bytesPerSec < 0) return "0 B/s"

        return when (mode) {
            FormattingMode.FORCE_KBS -> "%.1f KB/s".format(bytesPerSec / 1_024.0)
            FormattingMode.FORCE_MBS -> "%.2f MB/s".format(bytesPerSec / (1_024.0 * 1_024.0))
            FormattingMode.AUTO_BITS -> formatDynamicBits(bytesPerSec)
            FormattingMode.AUTO_BYTES -> formatDynamicBytes(bytesPerSec)
        }
    }

    private fun formatDynamicBytes(bytes: Long): String {
        if (bytes < 1_024) return "$bytes B/s"
        
        val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s", "TB/s")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt() // Calculates index bracket

        // Limits array overflow realistically avoiding crash over massive hardware resets
        val safeIndex = digitGroups.coerceIn(0, units.size - 1)

        val amount = bytes / 1024.0.pow(safeIndex.toDouble())
        return if (safeIndex == 0) "%.0f %s".format(amount, units[safeIndex]) // Don't float raw B/s
        else "%.1f %s".format(amount, units[safeIndex])
    }

    private fun formatDynamicBits(bytes: Long): String {
        val bits = bytes * 8 // Standard bit multiplication conversion
        if (bits < 1_000) return "$bits bps"

        // Networking fundamentally advertises base-10 metrics (Kbps = 1000 bps) rather than 1024.
        val units = arrayOf("bps", "Kbps", "Mbps", "Gbps")
        val digitGroups = (log10(bits.toDouble()) / log10(1000.0)).toInt()
        val safeIndex = digitGroups.coerceIn(0, units.size - 1)

        val amount = bits / 1000.0.pow(safeIndex.toDouble())
        return "%.1f %s".format(amount, units[safeIndex])
    }
}
