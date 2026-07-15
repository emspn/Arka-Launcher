package com.arka.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    packageName: String,
    size: Dp = 46.dp,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val theme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current
    val sizePx = with(density) { size.toPx() }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "press_scale"
    )

    val request = remember(packageName) {
        android.util.Log.d("AppIcon", "Creating request for: $packageName")
        ImageRequest.Builder(context)
            .data("app-icon://$packageName")
            .crossfade(true)
            .build()
    }

    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.primary.copy(alpha = 0.2f),
                        theme.surface
                    ),
                    center = Offset(sizePx * 0.32f, sizePx * 0.28f),
                    radius = sizePx * 0.7f
                )
            )
            .border(1.dp, theme.outline, CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isError) {
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.6f),
                contentScale = ContentScale.Fit,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) {
                        android.util.Log.e("AppIcon", "Error loading icon for $packageName: ${state.result.throwable}")
                        isError = true
                    }
                    if (state is AsyncImagePainter.State.Success) {
                        android.util.Log.d("AppIcon", "Successfully loaded icon for $packageName")
                    }
                }
            )
        } else {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = contentDescription,
                tint = theme.primary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}
