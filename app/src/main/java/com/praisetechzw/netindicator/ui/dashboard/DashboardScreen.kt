package com.praisetechzw.netindicator.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.praisetechzw.netindicator.domain.model.NetworkEvent
import com.praisetechzw.netindicator.domain.model.NetworkSnapshot
import com.praisetechzw.netindicator.domain.model.NetworkType
import com.praisetechzw.netindicator.ui.theme.SignalBad
import com.praisetechzw.netindicator.ui.theme.SignalExcellent
import com.praisetechzw.netindicator.ui.theme.SignalFair
import com.praisetechzw.netindicator.ui.theme.SignalGood
import com.praisetechzw.netindicator.ui.theme.SignalPoor
import com.praisetechzw.netindicator.utils.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val snapshot by viewModel.currentSnapshot.collectAsStateWithLifecycle()
    val events by viewModel.recentEvents.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Network Monitor",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
        }

        item {
            ConnectionStatusCard(snapshot)
        }

        item {
            SpeedCard(snapshot)
        }

        item {
            Text(
                text = "Recent Events",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (events.isEmpty()) {
            item {
                EmptyEventsPlaceholder()
            }
        } else {
            items(events, key = { it.id }) { event ->
                NetworkEventRow(event)
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(snapshot: NetworkSnapshot?) {
    val isConnected = snapshot?.isConnected == true
    val animatedColor by animateColorAsState(
        targetValue = if (isConnected) SignalExcellent else SignalBad,
        animationSpec = tween(600),
        label = "status_color"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pulse dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )
                    Text(
                        text = if (isConnected) "Connected" else "No Connection",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.height(12.dp))

                val icon = when {
                    snapshot == null -> Icons.Filled.WifiOff
                    snapshot.networkType == NetworkType.WIFI -> Icons.Filled.Wifi
                    snapshot.networkType == NetworkType.NONE -> Icons.Filled.WifiOff
                    isConnected -> Icons.Filled.SignalCellular4Bar
                    else -> Icons.Filled.SignalCellularOff
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = snapshot?.let {
                            when (it.networkType) {
                                NetworkType.WIFI -> it.ssid ?: "Wi-Fi"
                                NetworkType.NONE -> "Disconnected"
                                else -> it.networkOperator ?: it.networkType.name
                            }
                        } ?: "Waiting for data…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                snapshot?.ipAddress?.let { ip ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "IP: $ip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                snapshot?.pingMs?.let { ping ->
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Ping: ${FormatUtils.formatPing(ping)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "(${FormatUtils.pingQuality(ping)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = pingQualityColor(ping)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedCard(snapshot: NetworkSnapshot?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpeedMetric(
                label = "Download",
                value = FormatUtils.formatSpeed(snapshot?.downloadSpeedBps),
                arrow = "↓",
                color = MaterialTheme.colorScheme.primary
            )
            SpeedMetric(
                label = "Upload",
                value = FormatUtils.formatSpeed(snapshot?.uploadSpeedBps),
                arrow = "↑",
                color = MaterialTheme.colorScheme.tertiary
            )
            SpeedMetric(
                label = "Signal",
                value = snapshot?.signalStrength?.let { "${it}dBm" } ?: "–",
                arrow = "◈",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SpeedMetric(label: String, value: String, arrow: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = arrow, style = MaterialTheme.typography.titleMedium, color = color)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun NetworkEventRow(event: NetworkEvent) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isConnected)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = event.networkType.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.ssid ?: event.networkOperator ?: "–",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatter.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                event.pingMs?.let {
                    Text(
                        text = FormatUtils.formatPing(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = pingQualityColor(it)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyEventsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No events yet. Monitoring in progress…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun pingQualityColor(ms: Long): Color = when {
    ms < 20 -> SignalExcellent
    ms < 60 -> SignalGood
    ms < 120 -> SignalFair
    ms < 250 -> SignalPoor
    else -> SignalBad
}
