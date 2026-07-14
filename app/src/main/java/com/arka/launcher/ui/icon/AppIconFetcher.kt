package com.arka.launcher.ui.icon

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options

class AppIconFetcher(
    private val packageName: String,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val pm = context.packageManager
        return try {
            // Use getApplicationInfo + loadIcon for more reliable fetching on some OEMs
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val icon = appInfo.loadIcon(pm)
            
            DrawableResult(
                drawable = icon,
                isSampled = false,
                dataSource = DataSource.DISK
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("AppIconFetcher", "Package not found: $packageName")
            null
        } catch (e: Exception) {
            Log.e("AppIconFetcher", "Error fetching icon for $packageName", e)
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (!data.startsWith("app-icon://")) return null
            val packageName = data.substringAfter("app-icon://")
            if (packageName.isBlank()) return null
            return AppIconFetcher(packageName, context)
        }
    }
}
