package com.praisetechzw.netindicator.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.domain.model.DailyUsage
import com.praisetechzw.netindicator.domain.model.NetworkStats
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import com.praisetechzw.netindicator.domain.usecase.GetNetworkStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val stats: NetworkStats? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: NetworkRepository,
    private val getStatsUseCase: GetNetworkStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    val dailyUsage: StateFlow<List<DailyUsage>> =
        repository.observeDailyUsage(14)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadStats()
    }

    fun loadStats(days: Int = 7) {
        viewModelScope.launch {
            _uiState.value = StatsUiState(isLoading = true)
            val stats = getStatsUseCase(days)
            _uiState.value = StatsUiState(stats = stats, isLoading = false)
        }
    }
}
