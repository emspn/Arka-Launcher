package com.arka.launcher.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM installed_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<InstalledApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<InstalledApp>)

    @Query("DELETE FROM installed_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("DELETE FROM installed_apps")
    suspend fun clearAll()
}
