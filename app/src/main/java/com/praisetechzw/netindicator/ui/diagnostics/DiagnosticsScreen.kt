package com.praisetechzw.netindicator.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.praisetechzw.netindicator.engine.NetworkState
import com.praisetechzw.netindicator.ui.theme.SignalBad
import com.praisetechzw.netindicator.ui.theme.SignalExcellent
import com.praisetechzw.netindicator.ui.theme.SignalFair

@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Trigger explicit system checks dynamically when the user lands on the page mapping settings bounds.
    DisposableEffect(Unit) {
        viewModel.refreshSystemChecks(context)
        onDispose { }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "System Diagnostics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Troubleshoot background restrictions blocking monitoring limits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            DiagnosticSectionHeader("Critical Permissions")
            
            DiagnosticItemCard(
                title = "Notification Visibility",
                description = if (state.isNotificationsEnabled) "Foreground Notification Active" else "Blocked: Service cannot run persistently",
                icon = Icons.Default.NotificationsActive,
                statusColor = if (state.isNotificationsEnabled) SignalExcellent else SignalBad,
                onClick = { viewModel.openNotificationSettings(context) }
            )
            
            DiagnosticItemCard(
                title = "Battery Restrictions",
                description = if (state.isBatteryOptimized) "Optimized: Android may kill the engine" else "Unrestricted: Deep polling secure",
                icon = Icons.Default.BatteryAlert,
                statusColor = if (state.isBatteryOptimized) SignalBad else SignalExcellent,
                onClick = { viewModel.openBatteryOptimization(context) }
            )
            
            if (state.autoStartSupported) {
                DiagnosticItemCard(
                    title = "OEM Auto-Start Lock",
                    description = "Custom OS limits boot connections.",
                    icon = Icons.Default.Security,
                    statusColor = SignalFair,
                    onClick = { viewModel.openAutoStartSettings(context) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticSectionHeader("Engine Telemetry")
            
            DiagnosticItemCard(
                title = "Foreground Thread State",
                description = if (state.isServiceRunning) "Actively bound to OS limits" else "Engine halted cleanly",
                icon = Icons.Default.Settings,
                statusColor = if (state.isServiceRunning) SignalExcellent else SignalFair,
                onClick = null
            )
            
            DiagnosticItemCard(
                title = "Active Sensor Mode",
                description = state.networkState.name.replace("_", " "),
                icon = Icons.Default.Wifi,
                statusColor = if (state.networkState != NetworkState.DISCONNECTED && state.networkState != NetworkState.UNKNOWN) SignalExcellent else SignalBad,
                onClick = null
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticSectionHeader("Hardware Bounds")
            
            DiagnosticItemCard(
                title = "${state.deviceManufacturer} ${state.deviceModel}",
                description = "Android Version ${state.androidVersion}",
                icon = Icons.Default.PhoneAndroid,
                statusColor = Color.Gray,
                onClick = null
            )
        }
    }
}

@Composable
private fun DiagnosticSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DiagnosticItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    statusColor: Color,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (statusColor == SignalBad) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                if (onClick != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Fix Issue Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
