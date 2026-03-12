package com.praisetechzw.netindicator.ui.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.domain.model.DailyUsageSummary
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UsageViewModel @Inject constructor(
    repository: NetworkRepository
) : ViewModel() {

    // Track active usage stats for today securely linked via accurate StateFlow
    val todayUsage: StateFlow<DailyUsageSummary?> =
        repository.observeDailyUsage(LocalDate.now().toEpochDay())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Pull last 30 days history dynamically observing backend Room limits
    val recentHistory: StateFlow<List<DailyUsageSummary>> =
        repository.observeDailyUsageSequence(30)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
