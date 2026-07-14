package com.arka.launcher.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.R
import com.arka.launcher.ui.components.AppIcon
import com.arka.launcher.ui.components.ArkaWheel
import com.arka.launcher.ui.components.PrabhaScene
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSetDefaultLauncher: () -> Unit
) {
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val dockApps by viewModel.dockApps.collectAsState()
    val isPrabhaMode by viewModel.isPrabhaMode.collectAsState()
    val showDefaultLauncherPrompt by viewModel.showDefaultLauncherPrompt.collectAsState()

    if (apps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(theme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = theme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Initializing Arka...", color = theme.onBackground)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        KonarkSilhouette(modifier = Modifier.align(Alignment.BottomCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arka",
                    color = theme.secondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = { viewModel.togglePrabhaMode() },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPrabhaMode) theme.tertiary else theme.surface)
                        .border(1.dp, theme.outline, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_feather),
                        contentDescription = "Prabha Mode",
                        tint = theme.onSurface,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            if (showDefaultLauncherPrompt && !isPrabhaMode) {
                Spacer(modifier = Modifier.height(12.dp))
                DefaultLauncherBanner(
                    onSetDefault = onSetDefaultLauncher,
                    onDismiss = { viewModel.dismissDefaultLauncherPrompt() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Content Area
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = isPrabhaMode,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "mode_transition"
                    ) { prabha ->
                        if (prabha) {
                            PrabhaScene()
                        } else {
                            ArkaWheel()
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    ClockDisplay()
                    
                    if (isPrabhaMode) {
                        Text(
                            text = "Prabha Mode — distractions hidden",
                            color = theme.secondary,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }
                }
            }

            // Bottom UI
            AnimatedVisibility(
                visible = !isPrabhaMode,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.outline, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setLauncherState(LauncherState.DRAWER) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = theme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search Arka",
                            color = theme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .shadow(elevation = 10.dp, shape = RoundedCornerShape(28.dp))
                            .clip(RoundedCornerShape(28.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.outline, RoundedCornerShape(28.dp))
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        dockApps.forEach { app ->
                            AppIcon(
                                packageName = app.packageName,
                                onClick = {
                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (intent != null) {
                                        context.startActivity(intent)
                                    }
                                },
                                onLongClick = {
                                    viewModel.showAppMenu(app)
                                }
                            )
                        }
                        repeat(4 - dockApps.size) {
                            Box(modifier = Modifier.size(46.dp).background(theme.outline.copy(alpha = 0.2f), CircleShape))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun DefaultLauncherBanner(
    onSetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Set Arka as your home screen",
                    color = theme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Experience the full Konark theme",
                    color = theme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = theme.onSurfaceVariant, fontSize = 13.sp)
            }
            Button(
                onClick = onSetDefault,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Set default", color = theme.background, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ClockDisplay() {
    val theme = MaterialTheme.colorScheme
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeFormat.format(currentTime.time),
            color = theme.onBackground,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = dateFormat.format(currentTime.time).uppercase(),
            color = theme.onSurfaceVariant,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun KonarkSilhouette(modifier: Modifier = Modifier) {
    val copper = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxWidth().height(130.dp)) {
        val w = size.width
        val h = size.height
        val opacity = 0.05f

        val path = Path().apply {
            moveTo(w * 0.29f, h)
            lineTo(w * 0.32f, h * 0.77f)
            lineTo(w * 0.37f, h * 0.77f)
            lineTo(w * 0.39f, h * 0.6f)
            lineTo(w * 0.44f, h * 0.6f)
            lineTo(w * 0.46f, h * 0.46f)
            lineTo(w * 0.54f, h * 0.46f)
            lineTo(w * 0.56f, h * 0.6f)
            lineTo(w * 0.61f, h * 0.6f)
            lineTo(w * 0.63f, h * 0.77f)
            lineTo(w * 0.68f, h * 0.77f)
            lineTo(w * 0.71f, h)
            close()
        }
        drawPath(path, color = copper, alpha = opacity, style = Stroke(width = 1.4.dp.toPx()))
        
        drawRect(
            color = copper,
            topLeft = Offset(w * 0.26f, h * 0.97f),
            size = androidx.compose.ui.geometry.Size(w * 0.48f, 4.dp.toPx()),
            alpha = opacity,
            style = Stroke(width = 1.dp.toPx())
        )

        val centers = listOf(w * 0.39f, w * 0.5f, w * 0.61f)
        centers.forEach { cx ->
            val cy = h * 1.03f
            val r = 9.dp.toPx()
            drawCircle(color = copper, radius = r, center = Offset(cx, cy), alpha = opacity, style = Stroke(width = 1.dp.toPx()))
            for (i in 0 until 8) {
                val angle = (i / 8f) * 2 * Math.PI
                val x2 = cx + r * sin(angle).toFloat()
                val y2 = cy - r * cos(angle).toFloat()
                drawLine(
                    color = copper,
                    start = Offset(cx, cy),
                    end = Offset(x2, y2),
                    alpha = opacity,
                    strokeWidth = 0.6.dp.toPx()
                )
            }
        }
    }
}
