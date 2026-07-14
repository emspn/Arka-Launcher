package com.arka.launcher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArkaWheel(modifier: Modifier = Modifier) {
    val theme = MaterialTheme.colorScheme
    val copper = theme.primary
    val copperLight = theme.secondary
    val bg1 = theme.surface
    val bg2 = theme.surfaceVariant

    val rotation = remember { Animatable(0f) }
    var centerOffset by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()

    // Tracking for momentum
    var lastAngle by remember { mutableStateOf(0f) }
    var lastTime by remember { mutableLongStateOf(0L) }
    var velocity by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .size(236.dp)
            .onGloballyPositioned {
                centerOffset = Offset(it.size.width / 2f, it.size.height / 2f)
            }
            .pointerInput(Unit) {
                coroutineScope {
                    detectDragGestures(
                        onDragStart = { offset ->
                            scope.launch { rotation.stop() }
                            val dx = offset.x - centerOffset.x
                            val dy = offset.y - centerOffset.y
                            lastAngle = (atan2(dx, -dy) * 180 / PI).toFloat()
                            lastTime = System.currentTimeMillis()
                            velocity = 0f
                        },
                        onDrag = { change, _ ->
                            val currentPos = change.position
                            val dx = currentPos.x - centerOffset.x
                            val dy = currentPos.y - centerOffset.y
                            val angle = (atan2(dx, -dy) * 180 / PI).toFloat()
                            val now = System.currentTimeMillis()
                            
                            var delta = angle - lastAngle
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f

                            val dt = (now - lastTime).coerceAtLeast(1L)
                            val instVelocity = delta / dt
                            velocity = velocity * 0.7f + instVelocity * 0.3f
                            
                            scope.launch {
                                rotation.snapTo(rotation.value + delta)
                            }
                            
                            lastAngle = angle
                            lastTime = now
                        },
                        onDragEnd = {
                            scope.launch {
                                rotation.animateDecay(
                                    initialVelocity = velocity * 1000f, // deg/s
                                    animationSpec = exponentialDecay(frictionMultiplier = 1.5f)
                                )
                            }
                        }
                    )
                }
            }
            .graphicsLayer { rotationZ = rotation.value }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = canvasSize * 0.47f
            
            // Outer thin tick ring
            drawCircle(color = copper, radius = outerRadius, center = Offset(cx, cy), alpha = 0.3f, style = Stroke(0.8.dp.toPx()))
            
            // Minute ticks (60)
            for (i in 0 until 60) {
                val angleDeg = (i / 60f) * 360f
                val angleRad = (angleDeg - 90f) * PI / 180f
                val isMajor = i % 5 == 0
                val r1 = if (isMajor) outerRadius * 0.87f else outerRadius * 0.91f
                val r2 = outerRadius * 0.96f
                
                drawLine(
                    color = copper,
                    start = Offset(cx + r1.toFloat() * cos(angleRad).toFloat(), cy + r1.toFloat() * sin(angleRad).toFloat()),
                    end = Offset(cx + r2.toFloat() * cos(angleRad).toFloat(), cy + r2.toFloat() * sin(angleRad).toFloat()),
                    strokeWidth = if (isMajor) 1.4.dp.toPx() else 0.8.dp.toPx(),
                    alpha = if (isMajor) 0.95f else 0.55f
                )
            }

            // Outer rim and beads
            val rimRadius = outerRadius * 0.76f
            drawCircle(color = copper, radius = rimRadius, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
            
            for (i in 0 until 32) {
                val angleRad = (i / 32f) * 2 * PI
                val bx = cx + rimRadius * cos(angleRad).toFloat()
                val by = cy + rimRadius * sin(angleRad).toFloat()
                drawCircle(color = copperLight, radius = 1.6.dp.toPx(), center = Offset(bx, by), alpha = 0.8f)
            }

            // spokes
            for (i in 0 until 8) {
                // Major Spokes
                val majorAngleRad = (i / 8f) * 2 * PI - PI / 2
                val rInner = outerRadius * 0.38f
                val rOuter = outerRadius * 0.66f
                val rMed = outerRadius * 0.57f
                
                drawLine(
                    color = copper,
                    start = Offset(cx + rInner * cos(majorAngleRad).toFloat(), cy + rInner * sin(majorAngleRad).toFloat()),
                    end = Offset(cx + rOuter * cos(majorAngleRad).toFloat(), cy + rOuter * sin(majorAngleRad).toFloat()),
                    strokeWidth = 3.4.dp.toPx()
                )
                
                // Medallion
                val mx = cx + rMed * cos(majorAngleRad).toFloat()
                val my = cy + rMed * sin(majorAngleRad).toFloat()
                drawCircle(color = bg1, radius = 3.6.dp.toPx(), center = Offset(mx, my))
                drawCircle(color = copperLight, radius = 3.6.dp.toPx(), center = Offset(mx, my), style = Stroke(1.dp.toPx()))
                drawCircle(color = copperLight, radius = 1.1.dp.toPx(), center = Offset(mx, my))

                // Minor Spokes
                val minorAngleRad = ((i + 0.5f) / 8f) * 2 * PI - PI / 2
                val rMinorInner = outerRadius * 0.34f
                drawLine(
                    color = copper,
                    start = Offset(cx + rMinorInner * cos(minorAngleRad).toFloat(), cy + rMinorInner * sin(minorAngleRad).toFloat()),
                    end = Offset(cx + rOuter * cos(minorAngleRad).toFloat(), cy + rOuter * sin(minorAngleRad).toFloat()),
                    strokeWidth = 1.3.dp.toPx(),
                    alpha = 0.6f
                )
            }

            // Hub
            drawCircle(color = bg1, radius = outerRadius * 0.37f, center = Offset(cx, cy))
            drawCircle(color = copper, radius = outerRadius * 0.37f, center = Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
            drawCircle(color = copper, radius = outerRadius * 0.23f, center = Offset(cx, cy), alpha = 0.6f, style = Stroke(1.dp.toPx()))
            
            for (i in 0 until 8) {
                val angleRad = (i / 8f) * 2 * PI
                val r1 = outerRadius * 0.1f
                val r2 = outerRadius * 0.23f
                drawLine(
                    color = copper,
                    start = Offset(cx + r1 * cos(angleRad).toFloat(), cy + r1 * sin(angleRad).toFloat()),
                    end = Offset(cx + r2 * cos(angleRad).toFloat(), cy + r2 * sin(angleRad).toFloat()),
                    alpha = 0.7f,
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            drawCircle(color = bg2, radius = outerRadius * 0.07f, center = Offset(cx, cy))
            drawCircle(color = copperLight, radius = outerRadius * 0.07f, center = Offset(cx, cy), style = Stroke(1.2.dp.toPx()))
            drawCircle(color = copperLight, radius = outerRadius * 0.03f, center = Offset(cx, cy))
        }
    }
}
