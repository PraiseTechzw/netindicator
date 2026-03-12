package com.praisetechzw.netindicator.domain.usecase

import com.praisetechzw.netindicator.domain.repository.NetworkRepository
import javax.inject.Inject

class PruneOldDataUseCase @Inject constructor(
    private val repository: NetworkRepository
) {
    suspend operator fun invoke(retentionDays: Int) = repository.pruneOldData(retentionDays)
}
