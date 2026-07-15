package com.arka.launcher.ui.home

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.ui.theme.ArkaThemeColors
import com.arka.launcher.ui.theme.ArkaThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsSheet(
    onDismiss: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onOpenThemePicker: () -> Unit,
    currentThemeKey: String = "sandstone"
) {
    val context = LocalContext.current
    val theme = MaterialTheme.colorScheme
    val currentThemeColors = ArkaThemes[currentThemeKey]!!

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        contentColor = theme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = theme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Home screen",
                color = theme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 10.dp),
                letterSpacing = 2.sp
            )

            SettingsOption(
                text = "Wallpaper",
                onClick = {
                    onDismiss()
                    onOpenThemePicker()
                }
            )

            HorizontalDivider(color = theme.outline, thickness = 0.5.dp)

            SettingsOption(
                text = "Set Konark wallpaper",
                onClick = {
                    onDismiss()
                    setKonarkWallpaper(context, currentThemeColors)
                }
            )

            HorizontalDivider(color = theme.outline, thickness = 0.5.dp)

            SettingsOption(
                text = "Change default launcher",
                color = theme.tertiary, // Ember
                onClick = {
                    onDismiss()
                    onSetDefaultLauncher()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SettingsOption(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 13.sp
        )
    }
}

private fun setKonarkWallpaper(context: Context, theme: ArkaThemeColors) {
    try {
        val wm = WallpaperManager.getInstance(context)
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background Gradient
        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            theme.bg0.toArgb(), theme.bg1.toArgb(),
            Shader.TileMode.CLAMP
        )
        val paint = Paint().apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Subtle Temple Silhouette
        val copperPaint = Paint().apply {
            color = theme.copper.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2f
            alpha = (255 * 0.06f).toInt()
        }
        
        val w = width.toFloat()
        val h = height.toFloat()
        val bottom = h * 0.95f
        val templeHeight = 200f
        
        val path = android.graphics.Path().apply {
            moveTo(w * 0.2f, bottom)
            lineTo(w * 0.25f, bottom - templeHeight * 0.3f)
            lineTo(w * 0.35f, bottom - templeHeight * 0.3f)
            lineTo(w * 0.4f, bottom - templeHeight * 0.6f)
            lineTo(w * 0.45f, bottom - templeHeight * 0.6f)
            lineTo(w * 0.5f, bottom - templeHeight)
            lineTo(w * 0.55f, bottom - templeHeight * 0.6f)
            lineTo(w * 0.6f, bottom - templeHeight * 0.6f)
            lineTo(w * 0.65f, bottom - templeHeight * 0.3f)
            lineTo(w * 0.75f, bottom - templeHeight * 0.3f)
            lineTo(w * 0.8f, bottom)
        }
        canvas.drawPath(path, copperPaint)
        
        wm.setBitmap(bitmap)
        Toast.makeText(context, "Wallpaper applied", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
    }
}
