package com.praisetechzw.netindicator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.data.preferences.AppPreferencesDataSource
import com.praisetechzw.netindicator.data.preferences.AppSettings
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferencesDataSource,
    private val repository: NetworkRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        prefs.settingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setPollingInterval(seconds: Int) = viewModelScope.launch { prefs.setPollingInterval(seconds) }
    fun setRetentionDays(days: Int) = viewModelScope.launch { prefs.setRetentionDays(days) }
    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch { prefs.setDarkTheme(enabled) }
    fun setDynamicColors(enabled: Boolean) = viewModelScope.launch { prefs.setDynamicColors(enabled) }
    fun setShowNotifications(enabled: Boolean) = viewModelScope.launch { prefs.setShowNotifications(enabled) }
    fun setStartOnBoot(enabled: Boolean) = viewModelScope.launch { prefs.setStartOnBoot(enabled) }
    fun setPingHost(host: String) = viewModelScope.launch { prefs.setPingHost(host) }
    fun setBandwidthUnit(unit: String) = viewModelScope.launch { prefs.setBandwidthUnit(unit) }

    fun clearAllData() = viewModelScope.launch {
        repository.clearAll()
    }
}
