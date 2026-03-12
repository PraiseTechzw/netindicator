package com.praisetechzw.netindicator.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.praisetechzw.netindicator.domain.model.DailyUsageSummary
import com.praisetechzw.netindicator.engine.NetworkSpeed
import com.praisetechzw.netindicator.engine.NetworkState
import com.praisetechzw.netindicator.engine.SpeedFormatter
import com.praisetechzw.netindicator.engine.service.ServiceController
import com.praisetechzw.netindicator.ui.theme.SignalBad
import com.praisetechzw.netindicator.ui.theme.SignalExcellent
import com.praisetechzw.netindicator.utils.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Abstracting out manual intent bindings cleanly to the unified controller
    val serviceController = remember { ServiceController(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            ConnectionStatusCard(uiState.networkState)
        }

        item {
            SpeedTelemetryCard(uiState.currentSpeed)
        }
        
        item {
            DailyUsageCard(uiState.todayUsage)
        }

        item {
            ServiceControlAction(
                isMonitoring = uiState.isMonitoring,
                onToggle = {
                    if (uiState.isMonitoring) serviceController.stopMonitoring() 
                    else serviceController.startMonitoring()
                }
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(
            text = "NetPulse Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Real-time bandwidth precision",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ConnectionStatusCard(state: NetworkState) {
    val isConnected = state != NetworkState.DISCONNECTED && state != NetworkState.UNKNOWN
    
    val animatedColor by animateColorAsState(
        targetValue = if (isConnected) SignalExcellent else SignalBad,
        animationSpec = tween(600),
        label = "status_color"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )
                    Text(
                        text = if (isConnected) "Line Active" else "Offline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.height(16.dp))

                val icon = when (state) {
                    NetworkState.WIFI -> Icons.Filled.Wifi
                    NetworkState.DISCONNECTED -> Icons.Filled.WifiOff
                    else -> Icons.Filled.SettingsEthernet
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Adapter type",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = state.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedTelemetryCard(speed: NetworkSpeed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Total Throughput",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = SpeedFormatter.formatSpeed(speed.totalBytesPerSec),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpeedMetricData(
                    label = "Download",
                    value = SpeedFormatter.formatSpeed(speed.downloadBytesPerSec),
                    color = MaterialTheme.colorScheme.primary
                )
                SpeedMetricData(
                    label = "Upload",
                    value = SpeedFormatter.formatSpeed(speed.uploadBytesPerSec),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun SpeedMetricData(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            color = color
        )
    }
}

@Composable
private fun DailyUsageCard(usagePattern: DailyUsageSummary?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DataUsage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Today's Usage", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Wi-Fi + Cellular bounds", 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            val mappedPayload = if (usagePattern == null) "0 B" 
                else FormatUtils.formatBytes(usagePattern.totalDownloadBytes + usagePattern.totalUploadBytes)

            Text(
                text = mappedPayload,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Properly imported the width modifier

    
@Composable
private fun ServiceControlAction(
    isMonitoring: Boolean,
    onToggle: () -> Unit
) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isMonitoring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isMonitoring) "Stop Telemetry Engine" else "Start Deep Monitoring",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
