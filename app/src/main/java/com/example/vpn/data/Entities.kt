package com.example.vpn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_profiles")
data class VpnProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dnsServer: String,
    val isSelected: Boolean = false
)

@Entity(tableName = "app_bypass")
data class AppBypass(
    @PrimaryKey val packageName: String,
    val appName: String
)
