package com.arka.launcher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import com.arka.launcher.ui.icon.AppIconFetcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArkaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .components {
                add(AppIconFetcher.Factory(this@ArkaApplication))
            }
            .build()
    }
}
