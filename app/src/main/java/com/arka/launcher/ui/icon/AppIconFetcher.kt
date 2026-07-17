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
            val originalDrawable = pm.getApplicationIcon(appInfo)
            
            val size = 192
            val finalBitmap: Bitmap

            if (style == "natural") {
                finalBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(finalBitmap)
                originalDrawable.setBounds(0, 0, size, size)
                originalDrawable.draw(canvas)
            } else {
                // THEMED EXTRACTION ENGINE
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

                // Production-Grade Robust Extraction
                finalBitmap = performRobustExtraction(tempBitmap)
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

    private fun performRobustExtraction(src: Bitmap): Bitmap {
        val size = src.width
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        
        // 1. Analyze border pixels to detect solid background containers
        val perimeter = mutableListOf<Int>()
        for (i in 0 until size step 10) {
            perimeter.add(src.getPixel(i, 2))
            perimeter.add(src.getPixel(i, size - 3))
            perimeter.add(src.getPixel(2, i))
            perimeter.add(src.getPixel(size - 3, i))
        }
        
        var avgR = 0L; var avgG = 0L; var avgB = 0L; var avgA = 0L
        for (p in perimeter) {
            avgR += Color.red(p); avgG += Color.green(p); avgB += Color.blue(p); avgA += Color.alpha(p)
        }
        val meanR = (avgR / perimeter.size).toInt()
        val meanG = (avgG / perimeter.size).toInt()
        val meanB = (avgB / perimeter.size).toInt()
        val meanA = (avgA / perimeter.size).toInt()

        var logoPixelCount = 0
        
        for (x in 0 until size) {
            for (y in 0 until size) {
                val p = src.getPixel(x, y)
                val pA = Color.alpha(p)
                
                if (pA < 40) {
                    output.setPixel(x, y, Color.TRANSPARENT)
                    continue
                }

                // If icon has a solid border (like Chrome/ChatGPT white backgrounds)
                if (meanA > 200) {
                    val diff = Math.abs(Color.red(p) - meanR) + 
                               Math.abs(Color.green(p) - meanG) + 
                               Math.abs(Color.blue(p) - meanB)
                    
                    if (diff > 45) { // Significant difference from background
                        output.setPixel(x, y, Color.WHITE)
                        logoPixelCount++
                    } else {
                        output.setPixel(x, y, Color.TRANSPARENT)
                    }
                } else {
                    // Transparent-style foreground (ideal), use luminance
                    val brightness = (Color.red(p) * 0.299 + Color.green(p) * 0.587 + Color.blue(p) * 0.114).toInt()
                    if (brightness > 25) {
                        output.setPixel(x, y, Color.WHITE)
                        logoPixelCount++
                    } else {
                        output.setPixel(x, y, Color.TRANSPARENT)
                    }
                }
            }
        }

        // Final Recovery Pass: If the icon is inverted (logo is darker than background)
        // or if our extraction was too aggressive, use a high-contrast luminance stencil.
        if (logoPixelCount < (size * size * 0.005)) {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    val p = src.getPixel(x, y)
                    if (Color.alpha(p) > 50) {
                        val brightness = (Color.red(p) * 0.299 + Color.green(p) * 0.587 + Color.blue(p) * 0.114).toInt()
                        // If center is dark and background is light (Amazon style), invert.
                        if (meanR > 200 && brightness < 150) {
                            output.setPixel(x, y, Color.WHITE)
                        } else if (meanR < 50 && brightness > 100) {
                            output.setPixel(x, y, Color.WHITE)
                        }
                    }
                }
            }
        }
        
        return output
    }

    class Factory(private val context: Context) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): Fetcher? {
            val (packageName, style) = when (data) {
                is AppIconData -> data.packageName to data.style
                else -> null to "natural"
            }
            return if (packageName != null) AppIconFetcher(packageName, style, context) else null
        }
    }
}
