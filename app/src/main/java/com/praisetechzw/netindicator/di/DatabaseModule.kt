package com.praisetechzw.netindicator.di

import android.content.Context
import androidx.room.Room
import com.praisetechzw.netindicator.data.local.NetIndicatorDatabase
import com.praisetechzw.netindicator.data.local.dao.DailyUsageDao
import com.praisetechzw.netindicator.data.local.dao.NetworkEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NetIndicatorDatabase =
        Room.databaseBuilder(
            context,
            NetIndicatorDatabase::class.java,
            "net_indicator.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideNetworkEventDao(db: NetIndicatorDatabase): NetworkEventDao = db.networkEventDao()

    @Provides
    fun provideDailyUsageDao(db: NetIndicatorDatabase): DailyUsageDao = db.dailyUsageDao()
}
