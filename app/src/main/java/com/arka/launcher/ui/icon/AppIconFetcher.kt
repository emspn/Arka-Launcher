package com.arka.launcher.ui.icon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.util.LruCache
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

    companion object {
        // In-memory cache for icons to avoid repeated Bitmap creation and PM calls
        private val iconCache = LruCache<String, Bitmap>(50)
    }

    override suspend fun fetch(): FetchResult? {
        val cachedBitmap = iconCache.get(packageName)
        if (cachedBitmap != null) {
            return DrawableResult(
                drawable = BitmapDrawable(context.resources, cachedBitmap),
                isSampled = false,
                dataSource = DataSource.MEMORY
            )
        }

        val pm = context.packageManager
        return try {
            val drawable = pm.getApplicationIcon(packageName)
            
            // Standardize icon size for better performance (192x192 is ideal for high DPI)
            val size = 192
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            
            iconCache.put(packageName, bitmap)
            
            DrawableResult(
                drawable = BitmapDrawable(context.resources, bitmap),
                isSampled = false,
                dataSource = DataSource.DISK
            )
        } catch (e: Exception) {
            Log.e("ArkaIcon", "Failed fetching $packageName", e)
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): Fetcher? {
            val packageName = when (data) {
                is AppIconData -> data.packageName
                is String -> if (data.startsWith("app-icon://")) data.substringAfter("app-icon://") else null
                else -> null
            }
            return if (packageName != null) AppIconFetcher(packageName, context) else null
        }
    }
}
