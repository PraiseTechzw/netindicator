package com.praisetechzw.netindicator.engine.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.praisetechzw.netindicator.data.preferences.AppPreferencesDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Intercepts system reboot intents and package replacement triggers to re-activate the
 * continuous network tracker passively without user interaction if configured via Settings.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appPreferences: AppPreferencesDataSource

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i("NetPulse_Boot", "Received system broadcast interception: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED || 
            action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Detach work from the strict UI broadcast boundaries safely preventing ANR kills
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            
            scope.launch {
                try {
                    val settings = appPreferences.settingsFlow.first()
                    
                    if (settings.startOnBoot) {
                        Log.i("NetPulse_Boot", "Reboot tracking enabled via Settings. Re-binding Service Controller.")
                        ServiceController(context).startMonitoring()
                    } else {
                        Log.w("NetPulse_Boot", "Reboot tracking disabled. The service will wait for explicit manual launch.")
                    }
                } catch (e: Exception) {
                    Log.e("NetPulse_Boot", "Fatal crash resolving DataStore properties inside Boot Receiver", e)
                } finally {
                    // Critical memory cleanup releasing OS hold
                    pendingResult.finish()
                }
            }
        }
    }
}
