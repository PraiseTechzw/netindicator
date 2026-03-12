package com.praisetechzw.netindicator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.praisetechzw.netindicator.data.local.entity.DailyUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyUsageEntity)

    @Query("SELECT * FROM daily_usage ORDER BY dateEpochDay DESC LIMIT :limit")
    fun getRecentDays(limit: Int = 30): Flow<List<DailyUsageEntity>>

    @Query("SELECT * FROM daily_usage WHERE dateEpochDay = :day")
    suspend fun getForDay(day: Long): DailyUsageEntity?

    @Query("DELETE FROM daily_usage WHERE dateEpochDay < :olderThanDay")
    suspend fun deleteOlderThan(olderThanDay: Long)

    @Query("DELETE FROM daily_usage")
    suspend fun deleteAll()
}
