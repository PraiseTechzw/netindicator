package com.praisetechzw.netindicator.data.mapper

import com.praisetechzw.netindicator.data.local.entity.DailyUsageEntity
import com.praisetechzw.netindicator.data.local.entity.NetworkEventEntity
import com.praisetechzw.netindicator.domain.model.DailyUsage
import com.praisetechzw.netindicator.domain.model.NetworkEvent
import com.praisetechzw.netindicator.domain.model.NetworkSnapshot
import com.praisetechzw.netindicator.domain.model.NetworkType

fun NetworkEventEntity.toDomain(): NetworkEvent = NetworkEvent(
    id = id,
    timestamp = timestamp,
    networkType = NetworkType.entries.find { it.name == networkType } ?: NetworkType.UNKNOWN,
    ssid = ssid,
    signalStrength = signalStrength,
    downloadSpeedBps = downloadSpeedBps,
    uploadSpeedBps = uploadSpeedBps,
    pingMs = pingMs,
    isConnected = isConnected,
    ipAddress = ipAddress,
    networkOperator = networkOperator,
    linkSpeedMbps = linkSpeedMbps
)

fun NetworkSnapshot.toEntity(): NetworkEventEntity = NetworkEventEntity(
    timestamp = timestamp,
    networkType = networkType.name,
    ssid = ssid,
    signalStrength = signalStrength,
    downloadSpeedBps = downloadSpeedBps,
    uploadSpeedBps = uploadSpeedBps,
    pingMs = pingMs,
    isConnected = isConnected,
    ipAddress = ipAddress,
    networkOperator = networkOperator,
    linkSpeedMbps = linkSpeedMbps
)

fun DailyUsageEntity.toDomain(): DailyUsage = DailyUsage(
    date = date,
    totalDownloadBytes = totalDownloadBytes,
    totalUploadBytes = totalUploadBytes,
    avgPingMs = avgPingMs,
    avgDownloadBps = avgDownloadBps,
    avgUploadBps = avgUploadBps,
    sessionCount = sessionCount,
    wifiDurationMs = wifiDurationMs,
    mobileDurationMs = mobileDurationMs
)
