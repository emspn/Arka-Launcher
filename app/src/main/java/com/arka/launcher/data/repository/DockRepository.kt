package com.arka.launcher.data.repository

import android.content.Context
import android.util.Log
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
            Log.d("ArkaDock", "Raw dock string: '$packagesString'")
            packagesString.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
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
        val target = packageName.trim()
        Log.d("ArkaDock", "Pinning: $target")
        context.dataStore.edit { preferences ->
            val currentString = preferences[dockKey] ?: ""
            val currentList = currentString.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.equals(target, ignoreCase = true) }
                .toMutableList()
            
            currentList.add(target)
            
            val finalList = if (currentList.size > 4) currentList.takeLast(4) else currentList
            val newString = finalList.joinToString(",")
            preferences[dockKey] = newString
            Log.d("ArkaDock", "Saved string: '$newString'")
        }
    }

    suspend fun unpinApp(packageName: String) {
        val target = packageName.trim()
        Log.d("ArkaDock", "Unpinning: $target")
        context.dataStore.edit { preferences ->
            val currentString = preferences[dockKey] ?: ""
            val newList = currentString.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.equals(target, ignoreCase = true) }
            
            val newString = newList.joinToString(",")
            preferences[dockKey] = newString
            Log.d("ArkaDock", "Saved string after unpin: '$newString'")
        }
    }

    suspend fun reorderDock(newPackages: List<String>) {
        val newString = newPackages.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
        Log.d("ArkaDock", "Reordering to: $newString")
        context.dataStore.edit { preferences ->
            preferences[dockKey] = newString
        }
    }
}
