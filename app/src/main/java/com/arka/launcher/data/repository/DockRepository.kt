package com.arka.launcher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dock_prefs")

@Singleton
class DockRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dockKey = stringPreferencesKey("dock_packages")
    private val themeKey = stringPreferencesKey("theme_key")

    val dockPackages: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val packagesString = preferences[dockKey] ?: ""
            if (packagesString.isEmpty()) emptyList() else packagesString.split(",")
        }

    val selectedTheme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[themeKey] ?: "sandstone"
        }

    suspend fun setTheme(key: String) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = key
        }
    }

    suspend fun pinApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentString = preferences[dockKey] ?: ""
            val currentList = if (currentString.isEmpty()) emptyList() else currentString.split(",")
            
            // Remove if already exists to move to end/newest
            val newList = currentList.filter { it != packageName }.toMutableList()
            newList.add(packageName)
            
            // Keep only last 4
            val finalList = if (newList.size > 4) newList.takeLast(4) else newList
            
            preferences[dockKey] = finalList.joinToString(",")
        }
    }

    suspend fun unpinApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentString = preferences[dockKey] ?: ""
            val currentList = if (currentString.isEmpty()) emptyList() else currentString.split(",")
            val newList = currentList.filter { it != packageName }
            preferences[dockKey] = newList.joinToString(",")
        }
    }

    suspend fun reorderDock(newPackages: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[dockKey] = newPackages.joinToString(",")
        }
    }
}
