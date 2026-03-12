package com.praisetechzw.netindicator.domain.repository

import com.praisetechzw.netindicator.domain.model.DailyUsage
import com.praisetechzw.netindicator.domain.model.NetworkEvent
import com.praisetechzw.netindicator.domain.model.NetworkSnapshot
import com.praisetechzw.netindicator.domain.model.NetworkStats
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {

    /** Live stream of recent network events from the database. */
    fun observeRecentEvents(limit: Int = 200): Flow<List<NetworkEvent>>

    /** Live stream of daily usage for the history graph. */
    fun observeDailyUsage(days: Int = 30): Flow<List<DailyUsage>>

    /** Persist a new network snapshot to the database. */
    suspend fun saveSnapshot(snapshot: NetworkSnapshot)

    /** Compute aggregated stats over the last [days] days. */
    suspend fun getStats(days: Int = 7): NetworkStats

    /** Delete events older than [retentionDays] to manage storage. */
    suspend fun pruneOldData(retentionDays: Int)

    /** Clear all stored data. */
    suspend fun clearAll()
}
