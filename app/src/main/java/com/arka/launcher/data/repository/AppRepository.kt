package com.arka.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.arka.launcher.data.local.AppDao
import com.arka.launcher.data.local.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao
) {
    val allApps: Flow<List<InstalledApp>> = appDao.getAllApps()

    private var isRefreshing = false

    suspend fun refreshApps() = withContext(Dispatchers.IO) {
        if (isRefreshing) return@withContext
        isRefreshing = true
        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0) ?: emptyList()
            val newApps = resolveInfos.mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val appInfo = activityInfo.applicationInfo ?: return@mapNotNull null
                
                val packageName = activityInfo.packageName
                val appName = resolveInfo.loadLabel(pm).toString()
                val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledApp(packageName, appName, null, isSystemApp)
            }.sortedBy { it.packageName }

            val currentApps = appDao.getAllAppsSync().sortedBy { it.packageName }
            
            if (newApps != currentApps) {
                android.util.Log.d("ArkaRepository", "Apps changed, updating DB...")
                appDao.updateAppsTransaction(newApps)
            } else {
                android.util.Log.d("ArkaRepository", "No app changes detected.")
            }
        } finally {
            isRefreshing = false
        }
    }

    suspend fun addApp(packageName: String) = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val appName = pm.getApplicationLabel(appInfo).toString()
            val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            appDao.insertApps(listOf(InstalledApp(packageName, appName, null, isSystemApp)))
        } catch (e: PackageManager.NameNotFoundException) {
            // App not found
        }
    }

    suspend fun removeApp(packageName: String) = withContext(Dispatchers.IO) {
        appDao.deleteApp(packageName)
    }
}
