package com.arka.launcher.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dock_prefs")

@Singleton
class DockRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dockKey = stringPreferencesKey("dock_packages")
    private val themeKey = stringPreferencesKey("theme_key")
    private val streakKey = intPreferencesKey("focus_streak")
    private val lastFocusDateKey = longPreferencesKey("last_focus_date")
    private val quickAccessKey = stringPreferencesKey("quick_access_packages")
    private val iconStyleKey = stringPreferencesKey("icon_style")
    private val iconSizeKey = stringPreferencesKey("icon_size")

    val quickAccessPackages: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[quickAccessKey]?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() } ?: emptyList()
        }

    val iconStyle: Flow<String> = context.dataStore.data
        .map { it[iconStyleKey] ?: "natural" }

    val iconSize: Flow<String> = context.dataStore.data
        .map { it[iconSizeKey] ?: "normal" }

    suspend fun setIconStyle(style: String) {
        context.dataStore.edit { it[iconStyleKey] = style }
    }

    suspend fun setIconSize(size: String) {
        context.dataStore.edit { it[iconSizeKey] = size }
    }

    suspend fun addToQuickAccess(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[quickAccessKey] ?: ""
            val list = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(packageName)) {
                list.add(packageName)
                preferences[quickAccessKey] = list.joinToString(",")
            }
        }
    }

    suspend fun removeFromQuickAccess(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[quickAccessKey] ?: ""
            val newList = current.split(",").map { it.trim() }.filter { it.isNotEmpty() && it != packageName }
            preferences[quickAccessKey] = newList.joinToString(",")
        }
    }

    suspend fun reorderQuickAccess(newPackages: List<String>) {
        val newString = newPackages.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
        context.dataStore.edit { preferences ->
            preferences[quickAccessKey] = newString
        }
    }

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

    val focusStreak: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[streakKey] ?: 0
        }

    suspend fun recordFocusSession() {
        context.dataStore.edit { prefs ->
            val todayEpoch = LocalDate.now().toEpochDay()
            val lastDateEpoch = prefs[lastFocusDateKey] ?: 0L
            val currentStreak = prefs[streakKey] ?: 0

            if (lastDateEpoch == todayEpoch) return@edit

            if (lastDateEpoch == todayEpoch - 1) {
                prefs[streakKey] = currentStreak + 1
            } else {
                prefs[streakKey] = 1
            }
            prefs[lastFocusDateKey] = todayEpoch
        }
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
