package com.praisetechzw.netindicator.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.domain.model.DailyUsageSummary
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import com.praisetechzw.netindicator.engine.NetworkMonitor
import com.praisetechzw.netindicator.engine.NetworkSpeed
import com.praisetechzw.netindicator.engine.NetworkState
import com.praisetechzw.netindicator.engine.NetworkStateDetector
import com.praisetechzw.netindicator.engine.service.ServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val currentSpeed: NetworkSpeed = NetworkSpeed(0, 0, 0),
    val networkState: NetworkState = NetworkState.UNKNOWN,
    val isMonitoring: Boolean = false,
    val todayUsage: DailyUsageSummary? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NetworkRepository,
    networkMonitor: NetworkMonitor,
    stateDetector: NetworkStateDetector
) : ViewModel() {

    // Internal mutable state for the UI wrapper
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Collect real-time engine flows natively mapped into our UI state wrapper 
        viewModelScope.launch {
            combine(
                networkMonitor.speedFlow,
                stateDetector.networkStateFlow,
                ServiceController.isRunning,
                // Using hardcoded current date for immediate dashboard today fetch
                repository.observeDailyUsage(LocalDate.now().toEpochDay()) 
            ) { speed, state, isRunning, usage ->
                DashboardUiState(
                    currentSpeed = speed,
                    networkState = state,
                    isMonitoring = isRunning,
                    todayUsage = usage
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }
}
