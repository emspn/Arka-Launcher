package com.arka.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun ArkaWheel(modifier: Modifier = Modifier) {
    val theme = MaterialTheme.colorScheme
    val copper = theme.primary
    val copperLight = theme.secondary
    val bg1 = theme.surface
    val bg2 = theme.surfaceVariant
    val haptic = LocalHapticFeedback.current

    val rotation = remember { Animatable(0f) }
    var centerOffset by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()

    var isInteracting by remember { mutableStateOf(false) }

    // Entrance animation
    val entranceScale = remember { Animatable(0.6f) }
    val entranceAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        launch { entranceScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        launch { entranceAlpha.animateTo(1f, tween(800)) }
    }

    // Ambient Rotation
    LaunchedEffect(isInteracting) {
        if (!isInteracting) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(durationMillis = 140000, easing = LinearEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .size(236.dp)
            .onGloballyPositioned {
                centerOffset = Offset(it.size.width / 2f, it.size.height / 2f)
            }
            .graphicsLayer {
                scaleX = entranceScale.value
                scaleY = entranceScale.value
                alpha = entranceAlpha.value
                rotationZ = rotation.value
            }
            .pointerInput(centerOffset) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isInteracting = true
                    scope.launch { rotation.stop() }
                    
                    var lastAngle = (atan2(down.position.x - centerOffset.x, -(down.position.y - centerOffset.y)) * 180 / PI).toFloat()
                    var lastTime = System.currentTimeMillis()
                    var lastHapticRotation = rotation.value
                    var velocity = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        
                        if (change.pressed) {
                            val currentPos = change.position
                            val angle = (atan2(currentPos.x - centerOffset.x, -(currentPos.y - centerOffset.y)) * 180 / PI).toFloat()
                            val now = System.currentTimeMillis()
                            
                            var delta = angle - lastAngle
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f

                            val dt = (now - lastTime).coerceAtLeast(1L)
                            val instVelocity = delta / dt
                            velocity = velocity * 0.6f + instVelocity * 0.4f
                            
                            scope.launch {
                                rotation.snapTo(rotation.value + delta)
                                if (abs(rotation.value - lastHapticRotation) >= 12f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    lastHapticRotation = rotation.value
                                }
                            }
                            
                            lastAngle = angle
                            lastTime = now
                            change.consume()
                        } else {
                            // Momentum decay on release
                            val finalVelocity = velocity
                            scope.launch {
                                rotation.animateDecay(
                                    initialVelocity = finalVelocity * 1000f,
                                    animationSpec = exponentialDecay(frictionMultiplier = 1.2f)
                                )
                                isInteracting = false
                            }
                            break
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = canvasSize * 0.47f
            
            drawCircle(color = copper, radius = outerRadius, center = Offset(cx, cy), alpha = 0.3f, style = Stroke(0.8.dp.toPx()))
            
            for (i in 0 until 60) {
                val angleDeg = (i / 60f) * 360f
                val angleRad = (angleDeg - 90f) * PI / 180f
                val isMajor = i % 5 == 0
                val r1 = if (isMajor) outerRadius * 0.87f else outerRadius * 0.91f
                val r2 = outerRadius * 0.96f
                drawLine(
                    color = copper,
                    start = Offset(cx + (r1 * cos(angleRad)).toFloat(), cy + (r1 * sin(angleRad)).toFloat()),
                    end = Offset(cx + (r2 * cos(angleRad)).toFloat(), cy + (r2 * sin(angleRad)).toFloat()),
                    strokeWidth = if (isMajor) 1.4.dp.toPx() else 0.8.dp.toPx(),
                    alpha = if (isMajor) 0.95f else 0.55f
                )
            }

            val rimRadius = outerRadius * 0.76f
            drawCircle(color = copper, radius = rimRadius, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
            for (i in 0 until 32) {
                val angleRad = (i / 32f) * 2 * PI
                drawCircle(color = copperLight, radius = 1.6.dp.toPx(), center = Offset(cx + rimRadius * cos(angleRad).toFloat(), cy + rimRadius * sin(angleRad).toFloat()), alpha = 0.8f)
            }

            for (i in 0 until 8) {
                val majorAngleRad = (i / 8f) * 2 * PI - PI / 2
                val rInner = outerRadius * 0.38f
                val rOuter = outerRadius * 0.66f
                val rMed = outerRadius * 0.57f
                drawLine(color = copper, start = Offset(cx + rInner * cos(majorAngleRad).toFloat(), cy + rInner * sin(majorAngleRad).toFloat()), end = Offset(cx + rOuter * cos(majorAngleRad).toFloat(), cy + rOuter * sin(majorAngleRad).toFloat()), strokeWidth = 3.4.dp.toPx())
                val mx = cx + rMed * cos(majorAngleRad).toFloat()
                val my = cy + rMed * sin(majorAngleRad).toFloat()
                drawCircle(color = bg1, radius = 3.6.dp.toPx(), center = Offset(mx, my))
                drawCircle(color = copperLight, radius = 3.6.dp.toPx(), center = Offset(mx, my), style = Stroke(1.dp.toPx()))
                drawCircle(color = copperLight, radius = 1.1.dp.toPx(), center = Offset(mx, my))

                val minorAngleRad = ((i + 0.5f) / 8f) * 2 * PI - PI / 2
                val rMinorInner = outerRadius * 0.34f
                drawLine(color = copper, start = Offset(cx + rMinorInner * cos(minorAngleRad).toFloat(), cy + rMinorInner * sin(minorAngleRad).toFloat()), end = Offset(cx + rOuter * cos(minorAngleRad).toFloat(), cy + rOuter * sin(minorAngleRad).toFloat()), strokeWidth = 1.3.dp.toPx(), alpha = 0.6f)
            }

            drawCircle(color = bg1, radius = outerRadius * 0.37f, center = Offset(cx, cy))
            drawCircle(color = copper, radius = outerRadius * 0.37f, center = Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
            drawCircle(color = copper, radius = outerRadius * 0.23f, center = Offset(cx, cy), alpha = 0.6f, style = Stroke(1.dp.toPx()))
            for (i in 0 until 8) {
                val angleRad = (i / 8f) * 2 * PI
                val r1 = outerRadius * 0.1f
                val r2 = outerRadius * 0.23f
                drawLine(color = copper, start = Offset(cx + r1 * cos(angleRad).toFloat(), cy + r1 * sin(angleRad).toFloat()), end = Offset(cx + r2 * cos(angleRad).toFloat(), cy + r2 * sin(angleRad).toFloat()), alpha = 0.7f, strokeWidth = 1.dp.toPx())
            }
            drawCircle(color = bg2, radius = outerRadius * 0.07f, center = Offset(cx, cy))
            drawCircle(color = copperLight, radius = outerRadius * 0.07f, center = Offset(cx, cy), style = Stroke(1.2.dp.toPx()))
            drawCircle(color = copperLight, radius = outerRadius * 0.03f, center = Offset(cx, cy))
        }
    }
}
