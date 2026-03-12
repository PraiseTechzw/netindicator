package com.praisetechzw.netindicator.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.domain.model.NetworkEvent
import com.praisetechzw.netindicator.domain.model.NetworkSnapshot
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import com.praisetechzw.netindicator.service.NetworkMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val snapshot: NetworkSnapshot? = null,
    val recentEvents: List<NetworkEvent> = emptyList(),
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    /** Live snapshot from the foreground service shared StateFlow. */
    val currentSnapshot: StateFlow<NetworkSnapshot?> =
        NetworkMonitorService.currentSnapshot
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Last 50 events for the mini feed on the dashboard. */
    val recentEvents: StateFlow<List<NetworkEvent>> =
        repository.observeRecentEvents(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
