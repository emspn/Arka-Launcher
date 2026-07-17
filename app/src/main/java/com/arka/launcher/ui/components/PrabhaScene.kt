package com.arka.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PrabhaScene(modifier: Modifier = Modifier) {
    val theme = MaterialTheme.colorScheme
    val copper = theme.primary
    val copperLight = theme.secondary
    val bg0 = theme.background
    val bg1 = theme.surface
    val bg2 = theme.surfaceVariant
    val carve = theme.outline
    val ink = theme.onBackground

    val infiniteTransition = rememberInfiniteTransition(label = "prabha_animations")
    
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_breathe"
    )

    val chakraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(140000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chakra_spin"
    )

    Box(modifier = modifier
        .width(220.dp)
        .height(172.dp)
        .graphicsLayer(clip = true, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
        .background(bg0)
    ) {
        Spacer(modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height

                val farPath = Path().apply {
                    moveTo(0f, h * 0.68f)
                    lineTo(w * 0.08f, h * 0.58f)
                    lineTo(w * 0.19f, h * 0.66f)
                    lineTo(w * 0.29f, h * 0.5f)
                    lineTo(w * 0.4f, h * 0.63f)
                    lineTo(w * 0.5f, h * 0.47f)
                    lineTo(w * 0.6f, h * 0.64f)
                    lineTo(w * 0.71f, h * 0.53f)
                    lineTo(w * 0.81f, h * 0.66f)
                    lineTo(w * 0.92f, h * 0.55f)
                    lineTo(w, h * 0.67f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                val midPath = Path().apply {
                    moveTo(0f, h * 0.79f)
                    lineTo(w * 0.13f, h * 0.63f)
                    lineTo(w * 0.25f, h * 0.74f)
                    lineTo(w * 0.38f, h * 0.59f)
                    lineTo(w * 0.5f, h * 0.73f)
                    lineTo(w * 0.63f, h * 0.6f)
                    lineTo(w * 0.75f, h * 0.75f)
                    lineTo(w * 0.88f, h * 0.63f)
                    lineTo(w, h * 0.77f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                val nearPath = Path().apply {
                    moveTo(0f, h * 0.93f)
                    lineTo(w * 0.17f, h * 0.79f)
                    lineTo(w * 0.29f, h * 0.87f)
                    lineTo(w * 0.41f, h * 0.64f)
                    lineTo(w * 0.47f, h * 0.62f)
                    lineTo(w * 0.53f, h * 0.65f)
                    lineTo(w * 0.63f, h * 0.87f)
                    lineTo(w * 0.76f, h * 0.78f)
                    lineTo(w * 0.88f, h * 0.9f)
                    lineTo(w, h * 0.85f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                onDrawBehind {
                    // 1. Sky Gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to bg0,
                            0.65f to bg0,
                            1.0f to bg1
                        )
                    )

                    // 2. Sun Glow (Breathe)
                    val sunCenter = Offset(w * 0.49f, h * 0.37f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(copperLight.copy(alpha = 0.9f), copper.copy(alpha = 0.32f), Color.Transparent),
                            center = sunCenter,
                            radius = 58.dp.toPx()
                        ),
                        radius = 58.dp.toPx(),
                        center = sunCenter,
                        alpha = breatheAlpha
                    )
                    drawCircle(
                        color = copperLight,
                        radius = 19.dp.toPx(),
                        center = sunCenter,
                        alpha = 0.8f
                    )

                    // 3. Chakra (Slow Spin)
                    val chakraCenter = Offset(w * 0.77f, h * 0.21f)
                    withTransform({
                        rotate(chakraRotation, chakraCenter)
                    }) {
                        drawCircle(color = copper, radius = 14.dp.toPx(), center = chakraCenter, alpha = 0.35f, style = Stroke(1.dp.toPx()))
                        drawCircle(color = bg1, radius = 10.dp.toPx(), center = chakraCenter, alpha = 0.9f)
                        drawCircle(color = copperLight, radius = 10.dp.toPx(), center = chakraCenter, style = Stroke(1.dp.toPx()))
                        for (i in 0 until 8) {
                            val angle = (i / 8f) * 2 * PI
                            drawLine(
                                color = copperLight,
                                start = Offset(chakraCenter.x + 3.6.dp.toPx() * sin(angle).toFloat(), chakraCenter.y - 3.6.dp.toPx() * cos(angle).toFloat()),
                                end = Offset(chakraCenter.x + 9.dp.toPx() * sin(angle).toFloat(), chakraCenter.y - 9.dp.toPx() * cos(angle).toFloat()),
                                strokeWidth = 0.9.dp.toPx()
                            )
                        }
                        drawCircle(color = copperLight, radius = 1.6.dp.toPx(), center = chakraCenter)
                    }

                    // 4. Birds
                    val birdPath1 = Path().apply {
                        moveTo(w * 0.19f, h * 0.24f)
                        quadraticTo(w * 0.21f, h * 0.22f, w * 0.23f, h * 0.24f)
                        quadraticTo(w * 0.25f, h * 0.22f, w * 0.26f, h * 0.24f)
                    }
                    drawPath(birdPath1, color = copper, alpha = 0.6f, style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
                    
                    val birdPath2 = Path().apply {
                        moveTo(w * 0.25f, h * 0.31f)
                        quadraticTo(w * 0.265f, h * 0.29f, w * 0.28f, h * 0.31f)
                        quadraticTo(w * 0.295f, h * 0.29f, w * 0.31f, h * 0.31f)
                    }
                    drawPath(birdPath2, color = copper, alpha = 0.45f, style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))

                    // 5. Far Mountain Range
                    drawPath(
                        path = farPath,
                        brush = Brush.verticalGradient(listOf(copper.copy(alpha = 0.22f), carve.copy(alpha = 0.32f)))
                    )

                    // 6. Mid Mountain Range
                    drawPath(
                        path = midPath,
                        brush = Brush.verticalGradient(listOf(copper.copy(alpha = 0.35f), carve.copy(alpha = 0.65f)))
                    )

                    // 7. Near Peak (Monk's Seat)
                    drawPath(path = nearPath, brush = Brush.verticalGradient(listOf(bg2, bg0)))
                    drawPath(path = nearPath, color = carve, style = Stroke(1.dp.toPx()))

                    // Rim lighting on peak
                    val rimPath = Path().apply {
                        moveTo(w * 0.29f, h * 0.87f)
                        lineTo(w * 0.41f, h * 0.64f)
                        lineTo(w * 0.47f, h * 0.62f)
                        lineTo(w * 0.53f, h * 0.65f)
                        lineTo(w * 0.63f, h * 0.87f)
                    }
                    drawPath(rimPath, color = copperLight, alpha = 0.55f, style = Stroke(1.1.dp.toPx()))

                    // 8. The Monk
                    val monkX = w * 0.47f
                    drawCircle(color = ink, radius = 2.6.dp.toPx(), center = Offset(monkX, h * 0.43f)) // Head
                    drawCircle(color = ink, radius = 7.5.dp.toPx(), center = Offset(monkX, h * 0.47f)) // Torso upper
                    
                    val monkBase = Path().apply {
                        moveTo(w * 0.43f, h * 0.53f)
                        lineTo(w * 0.41f, h * 0.55f)
                        lineTo(w * 0.39f, h * 0.59f)
                        quadraticTo(w * 0.375f, h * 0.63f, w * 0.47f, h * 0.64f)
                        quadraticTo(w * 0.565f, h * 0.63f, w * 0.54f, h * 0.59f)
                        lineTo(w * 0.525f, h * 0.55f)
                        lineTo(w * 0.5f, h * 0.53f)
                        cubicTo(w * 0.5f, h * 0.55f, w * 0.485f, h * 0.56f, w * 0.47f, h * 0.56f)
                        cubicTo(w * 0.455f, h * 0.56f, w * 0.44f, h * 0.55f, w * 0.43f, h * 0.53f)
                    }
                    drawPath(monkBase, color = ink)
                    
                    // Arms
                    val leftArm = Path().apply {
                        moveTo(w * 0.425f, h * 0.53f)
                        quadraticTo(w * 0.38f, h * 0.57f, w * 0.37f, h * 0.62f)
                        quadraticTo(w * 0.365f, h * 0.635f, w * 0.39f, h * 0.63f)
                        quadraticTo(w * 0.41f, h * 0.59f, w * 0.445f, h * 0.55f)
                    }
                    drawPath(leftArm, color = ink)
                    
                    val rightArm = Path().apply {
                        moveTo(w * 0.51f, h * 0.53f)
                        quadraticTo(w * 0.555f, h * 0.57f, w * 0.565f, h * 0.62f)
                        quadraticTo(w * 0.57f, h * 0.635f, w * 0.545f, h * 0.63f)
                        quadraticTo(w * 0.52f, h * 0.59f, w * 0.485f, h * 0.55f)
                    }
                    drawPath(rightArm, color = ink)

                    // 9. Mist
                    drawRect(
                        color = bg1,
                        topLeft = Offset(0f, h * 0.83f),
                        size = Size(w, 20.dp.toPx()),
                        alpha = 0.3f
                    )

                    // 10. Vignette
                    drawRect(
                        brush = Brush.radialGradient(
                            0.6f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.28f),
                            center = Offset(w * 0.5f, h * 0.45f),
                            radius = h * 0.75f
                        )
                    )
                }
            }
        )
    }
}
