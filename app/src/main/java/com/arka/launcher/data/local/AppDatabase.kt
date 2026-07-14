package com.arka.launcher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InstalledApp::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
