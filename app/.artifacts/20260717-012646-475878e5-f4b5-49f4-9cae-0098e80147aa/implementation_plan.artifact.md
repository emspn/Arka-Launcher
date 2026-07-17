# Implement High Refresh Rate Sync

Sync the launcher's refresh rate with the device's maximum supported frequency to ensure all animations (Pager, Wheel, Custom Canvas) are as smooth as possible.

## Proposed Changes

### [Core UI]

#### [MainActivity.kt](file:///C:/Users/shakt/Desktop/Arka/app/src/main/java/com/arka/launcher/MainActivity.kt)

- Add a private method `setHighRefreshRate()` that queries the display for its supported modes and selects the one with the highest refresh rate.
- Update `window.attributes` to prefer this high-frequency mode.
- Call `setHighRefreshRate()` in `onCreate()` before `setContent`.

```kotlin
    private fun setHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display ?: return
                val modes = display.supportedModes
                if (modes.isEmpty()) return

                val maxRefreshRateMode = modes.maxByOrNull { it.refreshRate } ?: return

                val params = window.attributes
                params.preferredDisplayModeId = maxRefreshRateMode.modeId
                window.attributes = params
                Log.d("ArkaRefresh", "Set refresh rate to: ${maxRefreshRateMode.refreshRate}Hz (Mode ${maxRefreshRateMode.modeId})")
            } else {
                val params = window.attributes
                @Suppress("DEPRECATION")
                val display = windowManager.defaultDisplay
                val maxRate = display.supportedModes.maxByOrNull { it.refreshRate }?.refreshRate ?: 60f
                params.preferredRefreshRate = maxRate
                window.attributes = params
                Log.d("ArkaRefresh", "Set legacy refresh rate to: $maxRate Hz")
            }
        } catch (e: Exception) {
            Log.e("ArkaRefresh", "Failed to set refresh rate", e)
        }
    }
```

## Verification Plan

### Automated Tests
- N/A (Hardware specific behavior)

### Manual Verification
- Check `logcat` for the "ArkaRefresh" tag to see which refresh rate was targeted.
- Visually verify smoothness on a high-refresh-rate device (90Hz/120Hz).
- Use "Show Refresh Rate" in Android Developer Options to confirm the system is running at the requested frequency when Arka is in the foreground.
