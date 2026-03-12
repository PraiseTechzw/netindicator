package com.praisetechzw.netindicator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.praisetechzw.netindicator.data.local.dao.DailyUsageDao
import com.praisetechzw.netindicator.data.local.dao.NetworkEventDao
import com.praisetechzw.netindicator.data.local.entity.DailyUsageEntity
import com.praisetechzw.netindicator.data.local.entity.NetworkEventEntity

@Database(
    entities = [NetworkEventEntity::class, DailyUsageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NetIndicatorDatabase : RoomDatabase() {
    abstract fun networkEventDao(): NetworkEventDao
    abstract fun dailyUsageDao(): DailyUsageDao
}
