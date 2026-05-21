package com.example.vpn.di

import android.content.Context
import com.example.vpn.data.AppBypassDao
import com.example.vpn.data.AppDatabase
import com.example.vpn.data.VpnProfileDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideVpnProfileDao(database: AppDatabase): VpnProfileDao {
        return database.vpnProfileDao()
    }

    @Provides
    fun provideAppBypassDao(database: AppDatabase): AppBypassDao {
        return database.appBypassDao()
    }
}
