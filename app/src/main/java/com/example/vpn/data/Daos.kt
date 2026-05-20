package com.example.vpn.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnProfileDao {
    @Query("SELECT * FROM vpn_profiles")
    fun getAllProfiles(): Flow<List<VpnProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VpnProfile)

    @Update
    suspend fun updateProfile(profile: VpnProfile)

    @Delete
    suspend fun deleteProfile(profile: VpnProfile)

    @Query("UPDATE vpn_profiles SET isSelected = (id = :selectedId)")
    suspend fun selectProfile(selectedId: Int)

    @Query("SELECT * FROM vpn_profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedProfile(): VpnProfile?
}

@Dao
interface AppBypassDao {
    @Query("SELECT * FROM app_bypass")
    fun getAllBypassApps(): Flow<List<AppBypass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBypassApp(app: AppBypass)

    @Delete
    suspend fun deleteBypassApp(app: AppBypass)

    @Query("SELECT EXISTS(SELECT 1 FROM app_bypass WHERE packageName = :packageName)")
    suspend fun isAppBypassed(packageName: String): Boolean
}
