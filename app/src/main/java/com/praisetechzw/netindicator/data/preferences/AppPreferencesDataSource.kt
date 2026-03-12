package com.praisetechzw.netindicator.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netindicator_prefs")

data class AppSettings(
    val pollingIntervalSeconds: Int = 15,
    val retentionDays: Int = 30,
    val darkTheme: Boolean = false,
    val dynamicColors: Boolean = false,
    val showNotifications: Boolean = true,
    val pingHost: String = "8.8.8.8",
    val useMaterialYou: Boolean = true,
    val bandwidthUnit: String = "AUTO" // AUTO, KBPS, MBPS
)

@Singleton
class AppPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_POLLING_INTERVAL = intPreferencesKey("polling_interval")
        private val KEY_RETENTION_DAYS = intPreferencesKey("retention_days")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        private val KEY_SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        private val KEY_PING_HOST = stringPreferencesKey("ping_host")
        private val KEY_USE_MATERIAL_YOU = booleanPreferencesKey("use_material_you")
        private val KEY_BANDWIDTH_UNIT = stringPreferencesKey("bandwidth_unit")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            pollingIntervalSeconds = prefs[KEY_POLLING_INTERVAL] ?: 15,
            retentionDays = prefs[KEY_RETENTION_DAYS] ?: 30,
            darkTheme = prefs[KEY_DARK_THEME] ?: false,
            dynamicColors = prefs[KEY_DYNAMIC_COLORS] ?: false,
            showNotifications = prefs[KEY_SHOW_NOTIFICATIONS] ?: true,
            pingHost = prefs[KEY_PING_HOST] ?: "8.8.8.8",
            useMaterialYou = prefs[KEY_USE_MATERIAL_YOU] ?: true,
            bandwidthUnit = prefs[KEY_BANDWIDTH_UNIT] ?: "AUTO"
        )
    }

    suspend fun setPollingInterval(seconds: Int) {
        context.dataStore.edit { it[KEY_POLLING_INTERVAL] = seconds }
    }

    suspend fun setRetentionDays(days: Int) {
        context.dataStore.edit { it[KEY_RETENTION_DAYS] = days }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLORS] = enabled }
    }

    suspend fun setShowNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_NOTIFICATIONS] = enabled }
    }

    suspend fun setPingHost(host: String) {
        context.dataStore.edit { it[KEY_PING_HOST] = host }
    }

    suspend fun setUseMaterialYou(enabled: Boolean) {
        context.dataStore.edit { it[KEY_USE_MATERIAL_YOU] = enabled }
    }

    suspend fun setBandwidthUnit(unit: String) {
        context.dataStore.edit { it[KEY_BANDWIDTH_UNIT] = unit }
    }
}
