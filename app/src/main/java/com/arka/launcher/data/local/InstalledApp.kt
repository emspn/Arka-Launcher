package com.arka.launcher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_apps")
data class InstalledApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val iconUri: String? = null,
    val isSystemApp: Boolean = false
)
