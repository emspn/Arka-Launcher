package com.arka.launcher.ui.icon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
    private val style: String,
    private val context: Context
) : Fetcher {

    companion object {
        private val iconCache = LruCache<String, Bitmap>(150)
    }

    override suspend fun fetch(): FetchResult? {
        val cacheKey = "${packageName}_${style}"
        val cachedBitmap = iconCache.get(cacheKey)
        if (cachedBitmap != null) {
            return DrawableResult(
                drawable = BitmapDrawable(context.resources, cachedBitmap),
                isSampled = false,
                dataSource = DataSource.MEMORY
            )
        }

        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            var originalDrawable = pm.getApplicationIcon(appInfo)
            
            val size = 192
            val finalBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap)

            if (style == "natural") {
                originalDrawable.setBounds(0, 0, size, size)
                originalDrawable.draw(canvas)
            } else {
                // THEMED ICON PRODUCTION EXTRACTION
                var layer: Drawable? = null
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && originalDrawable is AdaptiveIconDrawable) {
                    layer = if (android.os.Build.VERSION.SDK_INT >= 33) originalDrawable.monochrome else null
                    if (layer == null) layer = originalDrawable.foreground
                } else {
                    layer = originalDrawable
                }

                val tempBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val tempCanvas = Canvas(tempBitmap)
                layer?.setBounds(0, 0, size, size)
                layer?.draw(tempCanvas)

                // The Magic: Extract only the logo based on contrast and brightness
                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                        // Greyscale conversion first
                        setSaturation(0f)
                        // Then move intensity to alpha channel and boost contrast
                        val scale = 2f
                        val translate = -100f
                        val m = arrayListOf(
                            1f, 1f, 1f, 0f, 0f,
                            1f, 1f, 1f, 0f, 0f,
                            1f, 1f, 1f, 0f, 0f,
                            0.33f, 0.59f, 0.11f, 0f, -60f // Sensitivity threshold
                        ).toFloatArray()
                        set(m)
                    })
                }
                
                canvas.drawBitmap(tempBitmap, 0f, 0f, paint)
                
                // If the extraction resulted in a mostly transparent bitmap, 
                // the icon logo was likely dark. We invert the logic.
                if (isBitmapMostlyEmpty(finalBitmap)) {
                    val invertPaint = Paint().apply {
                        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                            setSaturation(0f)
                            val m = arrayListOf(
                                1f, 1f, 1f, 0f, 0f,
                                1f, 1f, 1f, 0f, 0f,
                                1f, 1f, 1f, 0f, 0f,
                                -0.33f, -0.59f, -0.11f, 0f, 200f // Inverted threshold
                            ).toFloatArray()
                            set(m)
                        })
                    }
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    canvas.drawBitmap(tempBitmap, 0f, 0f, invertPaint)
                }
            }
            
            iconCache.put(cacheKey, finalBitmap)
            
            DrawableResult(
                drawable = BitmapDrawable(context.resources, finalBitmap),
                isSampled = false,
                dataSource = DataSource.DISK
            )
        } catch (e: Exception) {
            Log.e("ArkaIcon", "Failed processing $packageName", e)
            null
        }
    }

    private fun isBitmapMostlyEmpty(bitmap: Bitmap): Boolean {
        var opaquePixels = 0
        val sampleSize = 10
        for (x in 0 until bitmap.width step sampleSize) {
            for (y in 0 until bitmap.height step sampleSize) {
                if (Color.alpha(bitmap.getPixel(x, y)) > 30) {
                    opaquePixels++
                }
            }
        }
        // If less than 1% of the sampled area is opaque, it's considered empty/failed extraction
        val totalSamples = (bitmap.width / sampleSize) * (bitmap.height / sampleSize)
        return (opaquePixels.toFloat() / totalSamples.toFloat()) < 0.01f
    }

    class Factory(private val context: Context) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): Fetcher? {
            val (packageName, style) = when (data) {
                is AppIconData -> data.packageName to data.style
                is String -> {
                    if (data.startsWith("app-icon://")) {
                        data.substringAfter("app-icon://").substringBefore("?") to "natural"
                    } else null to "natural"
                }
                else -> null to "natural"
            }
            return if (packageName != null) AppIconFetcher(packageName, style, context) else null
        }
    }
}
