package com.arka.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.arka.launcher.ui.icon.AppIconData

@Composable
fun AppIcon(
    packageName: String,
    size: Dp = 46.dp,
    contentDescription: String? = null,
    style: String = "natural",
    sizeFactor: String = "normal",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier
) {
    val theme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current

    val factor = when (sizeFactor) {
        "small" -> 0.85f
        "large" -> 1.35f
        else -> 1f
    }
    val adjustedSize = size * factor
    val sizePx = with(density) { adjustedSize.toPx() }

    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "press_scale"
    )

    val request = remember(packageName, style) {
        ImageRequest.Builder(context)
            .data(AppIconData(packageName, style))
            .crossfade(200)
            .build()
    }

    Box(
        modifier = modifier
            .size(adjustedSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (style == "natural") {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .then(
                when (style) {
                    "natural" -> Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(theme.primary.copy(alpha = 0.15f), theme.surface),
                            center = Offset(sizePx * 0.3f, sizePx * 0.3f),
                            radius = sizePx * 0.8f
                        )
                    ).border(1.dp, theme.outline.copy(alpha = 0.5f), CircleShape)
                    "minimal" -> Modifier.background(theme.surface.copy(alpha = 0.45f))
                        .border(1.5.dp, theme.primary.copy(alpha = 0.2f), CircleShape)
                    else -> Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        var isError by remember { mutableStateOf(false) }

        if (isError) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = contentDescription,
                tint = theme.primary,
                modifier = Modifier.size(adjustedSize * 0.5f)
            )
        } else {
            // THEMED TINT: We use the theme primary color for non-natural icons.
            // Since the Fetcher now extracts a clean mask, this tint will look perfect.
            val tintFilter = if (style != "natural") ColorFilter.tint(theme.primary) else null
            
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                imageLoader = context.imageLoader,
                modifier = Modifier.size(adjustedSize * if (style == "ultraminimal") 0.85f else 0.72f),
                contentScale = ContentScale.Fit,
                colorFilter = tintFilter,
                onError = { isError = true }
            )
        }
    }
}
