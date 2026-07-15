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
        Log.d("AppIconFetcher", "fetch() called for package: $packageName")
        val pm = context.packageManager
        return try {
            // Use getApplicationInfo + loadIcon for more reliable fetching on some OEMs
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val icon = appInfo.loadIcon(pm)
            Log.d("AppIconFetcher", "Successfully loaded icon for $packageName")
            
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
            Log.d("AppIconFetcher", "Factory.create called with data: $data")
            if (!data.startsWith("app-icon://")) {
                Log.d("AppIconFetcher", "Data does not start with app-icon://, ignoring")
                return null
            }
            val packageName = data.substringAfter("app-icon://")
            if (packageName.isBlank()) {
                Log.w("AppIconFetcher", "Package name is blank for data: $data")
                return null
            }
            Log.d("AppIconFetcher", "Creating AppIconFetcher for $packageName")
            return AppIconFetcher(packageName, context)
        }
    }
}
