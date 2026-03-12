package com.praisetechzw.netindicator.ui.diagnostics

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.engine.NetworkState
import com.praisetechzw.netindicator.engine.NetworkStateDetector
import com.praisetechzw.netindicator.engine.service.ServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SystemDiagnosticsState(
    val isNotificationsEnabled: Boolean = false,
    val isBatteryOptimized: Boolean = true,
    val isServiceRunning: Boolean = false,
    val networkState: NetworkState = NetworkState.UNKNOWN,
    val autoStartSupported: Boolean = false,
    val androidVersion: String = Build.VERSION.RELEASE,
    val deviceManufacturer: String = Build.MANUFACTURER,
    val deviceModel: String = Build.MODEL
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val stateDetector: NetworkStateDetector
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemDiagnosticsState())
    val uiState: StateFlow<SystemDiagnosticsState> = _uiState.asStateFlow()

    init {
        // Collect dynamic state streams natively
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                stateDetector.networkStateFlow,
                ServiceController.isRunning
            ) { state, running ->
                _uiState.value.copy(
                    networkState = state,
                    isServiceRunning = running
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    /** Re-calculates exact system permission bounds instantly on resume */
    fun refreshSystemChecks(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val notificationManager = NotificationManagerCompat.from(context)

        val isBatteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            false
        }

        val autoStartSupported = Build.MANUFACTURER.lowercase() in listOf(
            "xiaomi", "oppo", "vivo", "letv", "honor", "realme", "oneplus"
        )

        _uiState.value = _uiState.value.copy(
            isNotificationsEnabled = notificationManager.areNotificationsEnabled(),
            isBatteryOptimized = isBatteryOptimized,
            autoStartSupported = autoStartSupported
        )
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openBatteryOptimization(context: Context) {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            } else {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
    }

    fun openAutoStartSettings(context: Context) {
        try {
            val intent = Intent().apply {
                when (Build.MANUFACTURER.lowercase()) {
                    "xiaomi" -> component = android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                    "oppo" -> component = android.content.ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                    "vivo" -> component = android.content.ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                    "honor" -> component = android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard app info if deep link fails
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }
}
