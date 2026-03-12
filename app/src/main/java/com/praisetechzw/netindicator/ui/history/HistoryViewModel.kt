package com.praisetechzw.netindicator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.praisetechzw.netindicator.domain.model.NetworkEvent
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: NetworkRepository
) : ViewModel() {

    val events: StateFlow<List<NetworkEvent>> =
        repository.observeRecentEvents(200)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
