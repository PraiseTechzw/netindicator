package com.praisetechzw.netindicator.di

import android.content.Context
import com.praisetechzw.netindicator.engine.NetworkMonitor
import com.praisetechzw.netindicator.engine.NetworkStateDetector
import com.praisetechzw.netindicator.engine.TrafficStatsNetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor {
        // Shared instance using Dispatchers.Default for internal looping bounds
        return TrafficStatsNetworkMonitor() 
    }

    @Provides
    @Singleton
    fun provideNetworkStateDetector(@ApplicationContext context: Context): NetworkStateDetector {
        // Shared instance reading OS transport connections via system service
        return NetworkStateDetector(context)
    }
}
