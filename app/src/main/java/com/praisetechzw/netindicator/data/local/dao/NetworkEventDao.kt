package com.praisetechzw.netindicator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.praisetechzw.netindicator.data.local.entity.NetworkEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: NetworkEventEntity): Long

    @Query("SELECT * FROM network_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 200): Flow<List<NetworkEventEntity>>

    @Query("SELECT * FROM network_events WHERE timestamp >= :fromEpoch ORDER BY timestamp DESC")
    fun getEventsSince(fromEpoch: Long): Flow<List<NetworkEventEntity>>

    @Query("SELECT * FROM network_events WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getEventsBetween(from: Long, to: Long): Flow<List<NetworkEventEntity>>

    @Query("SELECT COUNT(*) FROM network_events")
    suspend fun getCount(): Int

    @Query("DELETE FROM network_events WHERE timestamp < :olderThanEpoch")
    suspend fun deleteOlderThan(olderThanEpoch: Long)

    @Query("DELETE FROM network_events")
    suspend fun deleteAll()

    @Query("""
        SELECT AVG(downloadSpeedBps) FROM network_events
        WHERE timestamp >= :fromEpoch AND downloadSpeedBps IS NOT NULL
    """)
    suspend fun getAvgDownloadSince(fromEpoch: Long): Double?

    @Query("""
        SELECT AVG(uploadSpeedBps) FROM network_events
        WHERE timestamp >= :fromEpoch AND uploadSpeedBps IS NOT NULL
    """)
    suspend fun getAvgUploadSince(fromEpoch: Long): Double?

    @Query("""
        SELECT AVG(pingMs) FROM network_events
        WHERE timestamp >= :fromEpoch AND pingMs IS NOT NULL
    """)
    suspend fun getAvgPingSince(fromEpoch: Long): Double?
}
