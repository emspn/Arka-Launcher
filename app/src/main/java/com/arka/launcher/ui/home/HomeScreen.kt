package com.arka.launcher.ui.home

import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.arka.launcher.R
import com.arka.launcher.data.local.InstalledApp
import com.arka.launcher.ui.components.AppIcon
import com.arka.launcher.ui.components.ArkaWheel
import com.arka.launcher.ui.components.PrabhaScene
import kotlinx.coroutines.coroutineScope
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
    val apps by viewModel.apps.collectAsState()
    val dockApps by viewModel.dockApps.collectAsState()
    val isPrabhaMode by viewModel.isPrabhaMode.collectAsState()
    val showDefaultLauncherPrompt by viewModel.showDefaultLauncherPrompt.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Background long press for settings - ONLY trigger if not consumed by children
                detectTapGestures(
                    onLongPress = {
                        viewModel.showHomeSettings(true)
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -20f) { // Swipe up
                        viewModel.setLauncherState(LauncherState.DRAWER)
                    }
                }
            }
    ) {
        KonarkSilhouette(modifier = Modifier.align(Alignment.BottomCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.cycleTheme() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.outline, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Cycle Theme",
                            tint = theme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                            contentDescription = "Toggle Prabha Focus Mode",
                            tint = theme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            if (showDefaultLauncherPrompt && !isPrabhaMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    DefaultLauncherBanner(
                        onSetDefault = onSetDefaultLauncher,
                        onDismiss = { viewModel.dismissDefaultLauncherPrompt() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Content Area (Pager)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isPrabhaMode) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PrabhaScene()
                        Spacer(modifier = Modifier.height(14.dp))
                        ClockDisplay()
                        Text(
                            text = "Prabha Mode — distractions hidden",
                            color = theme.secondary,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) { page ->
                            if (page == 0) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    ArkaWheel()
                                    Spacer(modifier = Modifier.height(14.dp))
                                    ClockDisplay()
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    WeatherWidget()
                                    ClockWidget()
                                    PrabhaStatsWidget()
                                    DailyVerseWidget()
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }

                        // Page Dots
                        Row(
                            Modifier
                                .height(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(2) { iteration ->
                                val color = if (pagerState.currentPage == iteration) theme.primary else theme.outline
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(if (pagerState.currentPage == iteration) 16.dp else 6.dp, 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom UI (Hidden in Prabha Mode and on Widget Page)
            AnimatedVisibility(
                visible = !isPrabhaMode && pagerState.currentPage == 0,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Search Bar Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.outline, RoundedCornerShape(16.dp))
                            .clickable(
                                onClickLabel = "Open App Drawer"
                            ) { viewModel.setLauncherState(LauncherState.DRAWER) }
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

                    // Dock
                    DraggableDock(
                        viewModel = viewModel,
                        dockApps = dockApps
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun DraggableDock(
    viewModel: HomeViewModel,
    dockApps: List<InstalledApp>
) {
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    // We use the actual dockApps from the ViewModel to ensure it stays in sync
    var currentList by remember(dockApps) { mutableStateOf(dockApps) }
    var draggedPkg by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    val itemPositions = remember { mutableStateMapOf<String, Float>() }

    Row(
        modifier = Modifier
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(theme.surface)
            .border(1.dp, theme.outline, RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .pointerInput(Unit) {
                // EXCLUDE Background Settings: Consume long presses in dock row to prevent wallpaper menu
                detectTapGestures(onLongPress = { }) 
            },
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 4) {
            val app = currentList.getOrNull(i)
            if (app != null) {
                key(app.packageName) {
                    val isDragging = draggedPkg == app.packageName
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { layout ->
                                itemPositions[app.packageName] = layout.positionInParent().x
                            }
                            .zIndex(if (isDragging) 10f else 1f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationX = dragOffset
                                    scaleX = 1.25f
                                    scaleY = 1.25f
                                }
                            }
                            .pointerInput(app.packageName) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    val timeout = viewConfiguration.longPressTimeoutMillis
                                    val slop = viewConfiguration.touchSlop
                                    
                                    var isLongPress = false
                                    var movedSignificantly = false
                                    
                                    try {
                                        withTimeout(timeout) {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.first()
                                                if (change.pressed.not()) {
                                                    // RELEASE before timeout = TAP
                                                    Log.d("ArkaDock", "Open: ${app.packageName}")
                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                    intent?.let {
                                                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(it)
                                                    }
                                                    change.consume()
                                                    return@withTimeout
                                                }
                                                if ((change.position - down.position).getDistance() > slop) {
                                                    return@withTimeout // Cancel if moved too much early
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isLongPress = true
                                    }

                                    if (isLongPress) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // DO NOT show menu here. Wait for release.
                                        
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.first()
                                            
                                            if (change.pressed.not()) {
                                                if (!movedSignificantly) {
                                                    // LONG PRESS RELEASED WITHOUT DRAG -> SHOW MENU
                                                    viewModel.showAppMenu(app)
                                                } else {
                                                    // DRAG RELEASED -> COMMIT NEW ORDER
                                                    viewModel.reorderDock(currentList.map { it.packageName })
                                                }
                                                draggedPkg = null
                                                dragOffset = 0f
                                                change.consume()
                                                break
                                            }

                                            val xDelta = change.positionChange().x
                                            if (xDelta != 0f) {
                                                movedSignificantly = true
                                                draggedPkg = app.packageName
                                                dragOffset += xDelta
                                                change.consume()
                                                
                                                val currentIdx = currentList.indexOfFirst { it.packageName == app.packageName }
                                                val iconWidthPx = with(density) { 46.dp.toPx() }
                                                val currentX = (itemPositions[app.packageName] ?: 0f) + dragOffset + (iconWidthPx / 2)
                                                
                                                for (idx in currentList.indices) {
                                                    if (idx != currentIdx) {
                                                        val otherPkg = currentList[idx].packageName
                                                        val otherCenterX = (itemPositions[otherPkg] ?: 0f) + (iconWidthPx / 2)
                                                        
                                                        if ((currentIdx < idx && currentX > otherCenterX) || 
                                                            (currentIdx > idx && currentX < otherCenterX)) {
                                                            
                                                            val newList = currentList.toMutableList()
                                                            Collections.swap(newList, currentIdx, idx)
                                                            currentList = newList
                                                            
                                                            val oldPos = itemPositions[app.packageName] ?: 0f
                                                            val newPos = itemPositions[otherPkg] ?: 0f
                                                            dragOffset -= (newPos - oldPos)
                                                            
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        AppIcon(packageName = app.packageName)
                    }
                }
            } else {
                // Empty slot with Plus Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(theme.outline.copy(alpha = 0.15f))
                        .clickable { viewModel.setLauncherState(LauncherState.DRAWER) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add App",
                        tint = theme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherWidget() {
    val theme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Today", color = theme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("28°C", color = theme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Clear skies", color = theme.onSurfaceVariant, fontSize = 11.sp)
            }
            Icon(
                imageVector = Icons.Rounded.Cloud,
                contentDescription = "Weather: Clear skies",
                tint = theme.secondary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun ClockWidget() {
    val theme = MaterialTheme.colorScheme
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp, 20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeFormat.format(currentTime.time),
                color = theme.onSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(currentTime.time).uppercase(),
                color = theme.onSurfaceVariant,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun PrabhaStatsWidget() {
    val theme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp, 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("Prabha stats", color = theme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("demo data", color = theme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 8.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            StatRow("Screen time", "2h 14m")
            StatRow("Notifications", "6")
            StatRow("Focus streak", "3 days")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    val theme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = theme.onSurface, fontSize = 12.sp)
        Text(value, color = theme.secondary, fontSize = 12.sp)
    }
}

@Composable
fun DailyVerseWidget() {
    val theme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp, 20.dp)) {
            Text("Daily verse", color = theme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"The sun never says to the earth, you owe me.\"",
                color = theme.onSurface,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 22.sp
            )
            Text("— Rumi", color = theme.onSurfaceVariant, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
