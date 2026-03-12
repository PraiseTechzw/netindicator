package com.praisetechzw.netindicator.domain.usecase

import com.praisetechzw.netindicator.domain.model.NetworkStats
import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import javax.inject.Inject

class GetNetworkStatsUseCase @Inject constructor(
    private val repository: NetworkRepository
) {
    suspend operator fun invoke(days: Int = 7): NetworkStats = repository.getStats(days)
}
