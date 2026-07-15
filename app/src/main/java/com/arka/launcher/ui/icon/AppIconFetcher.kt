package com.arka.launcher.ui.icon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
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
        Log.d("ArkaIcon", "fetch() for $packageName")
        val pm = context.packageManager
        return try {
            val drawable = pm.getApplicationIcon(packageName)
            
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            
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
